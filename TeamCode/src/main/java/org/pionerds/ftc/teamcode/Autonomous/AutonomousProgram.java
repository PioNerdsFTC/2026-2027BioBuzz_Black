package org.pionerds.ftc.teamcode.Autonomous;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An autonomous routine built in the dashboard: a starting position, actions to run before moving,
 * and an ordered list of waypoints, each with actions that run once the robot reaches it.
 * All positions are in Coordination units (0-100). Immutable, so it is safe to share across threads.
 */
public class AutonomousProgram {
    public static final double MAX_DURATION_MS = 30000;
    public static final int MAX_SHOTS = 8;

    public static final AutonomousProgram EMPTY =
            new AutonomousProgram(null, Collections.emptyList(), Collections.emptyList());

    /**
     * What the robot should do, not how: Autonomous turns these into motor and servo commands,
     * including launcher spin-up and feed timing.
     */
    public static class Action {
        public enum Type {
            SHOOT,
            INTAKE,
            WAIT
        }

        public final Type type;

        /** SHOOT: "pollen" or "nectar". */
        public final String piece;

        /** SHOOT: how many pieces to launch. */
        public final int count;

        /** SHOOT: hood angle in degrees. INTAKE and WAIT: duration in ms. */
        public final double value;

        Action(Type type, String piece, int count, double value) {
            this.type = type;
            this.piece = piece;
            this.count = count;
            this.value = value;
        }
    }

    public static class Step {
        public final double x;
        public final double y;
        public final double tolerance;

        /** Run the intake while driving to this waypoint. */
        public final boolean intake;
        public final List<Action> actions;

        public Step(double x, double y, double tolerance, boolean intake, List<Action> actions) {
            this.x = x;
            this.y = y;
            this.tolerance = tolerance;
            this.intake = intake;
            this.actions = Collections.unmodifiableList(new ArrayList<>(actions));
        }
    }

    /** {x, y}, or null to keep Coordination's default starting position. */
    public final double[] start;
    public final List<Action> startActions;
    public final List<Step> steps;

    AutonomousProgram(double[] start, List<Action> startActions, List<Step> steps) {
        this.start = start == null ? null : start.clone();
        this.startActions = Collections.unmodifiableList(new ArrayList<>(startActions));
        this.steps = Collections.unmodifiableList(new ArrayList<>(steps));
    }

    public static AutonomousProgram fromJson(String body) throws JSONException {
        if (body == null) throw new JSONException("Missing request body");

        JSONObject json = new JSONObject(body);

        double[] start = null;
        if (json.has("start") && !json.isNull("start")) {
            JSONObject point = json.getJSONObject("start");
            start = new double[] { coordinate(point, "x", "start"), coordinate(point, "y", "start") };
        }

        List<Action> startActions = parseActions(json.optJSONArray("startActions"), "start");

        JSONArray waypoints = json.getJSONArray("waypoints");
        ArrayList<Step> steps = new ArrayList<>();

        for (int i = 0; i < waypoints.length(); i++) {
            JSONObject point = waypoints.getJSONObject(i);
            String where = "waypoint " + (i + 1);

            double tolerance = point.getDouble("tolerance");
            if (!(tolerance > 0 && tolerance <= 50)) throw new JSONException(where + " tolerance is out of range");

            steps.add(new Step(
                    coordinate(point, "x", where),
                    coordinate(point, "y", where),
                    tolerance,
                    point.optBoolean("intake", false),
                    parseActions(point.optJSONArray("actions"), where)
            ));
        }

        return new AutonomousProgram(start, startActions, steps);
    }

    public String toJson() {
        try {
            JSONObject json = new JSONObject();

            if (start != null) {
                json.put("start", new JSONObject().put("x", start[0]).put("y", start[1]));
            } else {
                json.put("start", JSONObject.NULL);
            }

            json.put("startActions", actionsToJson(startActions));

            JSONArray waypoints = new JSONArray();
            for (Step step : steps) {
                waypoints.put(new JSONObject()
                        .put("x", step.x)
                        .put("y", step.y)
                        .put("tolerance", step.tolerance)
                        .put("intake", step.intake)
                        .put("actions", actionsToJson(step.actions)));
            }

            return json.put("waypoints", waypoints).toString();
        } catch (JSONException e) {
            // Only thrown for non-finite numbers, which fromJson never accepts
            throw new IllegalStateException(e);
        }
    }

    private static double coordinate(JSONObject point, String key, String where) throws JSONException {
        double value = point.getDouble(key);
        if (!(value >= 0 && value <= 100)) throw new JSONException(where + " " + key + " is out of range");
        return value;
    }

    private static List<Action> parseActions(JSONArray array, String where) throws JSONException {
        ArrayList<Action> actions = new ArrayList<>();
        if (array == null) return actions;

        for (int i = 0; i < array.length(); i++) {
            JSONObject action = array.getJSONObject(i);
            String type = action.getString("type");

            switch (type) {
                case "shoot": {
                    String piece = action.getString("piece");
                    if (!piece.equals("pollen") && !piece.equals("nectar")) {
                        throw new JSONException(where + " has an unknown game piece '" + piece + "'");
                    }

                    int count = action.getInt("count");
                    if (count < 1 || count > MAX_SHOTS) throw new JSONException(where + " shot count is out of range");

                    double angle = action.getDouble("angle");
                    if (!(angle >= 0 && angle <= 180)) throw new JSONException(where + " hood angle is out of range");

                    actions.add(new Action(Action.Type.SHOOT, piece, count, angle));
                    break;
                }
                case "intake":
                    actions.add(new Action(Action.Type.INTAKE, null, 0, duration(action, where)));
                    break;
                case "wait":
                    actions.add(new Action(Action.Type.WAIT, null, 0, duration(action, where)));
                    break;
                default:
                    throw new JSONException(where + " has an unknown action '" + type + "'");
            }
        }

        return actions;
    }

    private static double duration(JSONObject action, String where) throws JSONException {
        double duration = action.getDouble("duration");
        if (!(duration >= 0 && duration <= MAX_DURATION_MS)) throw new JSONException(where + " duration is out of range");
        return duration;
    }

    private static JSONArray actionsToJson(List<Action> actions) throws JSONException {
        JSONArray array = new JSONArray();

        for (Action action : actions) {
            JSONObject json = new JSONObject();

            switch (action.type) {
                case SHOOT:
                    json.put("type", "shoot").put("piece", action.piece).put("count", action.count).put("angle", action.value);
                    break;
                case INTAKE:
                    json.put("type", "intake").put("duration", action.value);
                    break;
                case WAIT:
                    json.put("type", "wait").put("duration", action.value);
                    break;
            }

            array.put(json);
        }

        return array;
    }
}
