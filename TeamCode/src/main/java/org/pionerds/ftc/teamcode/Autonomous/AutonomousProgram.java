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

    public static final AutonomousProgram EMPTY =
            new AutonomousProgram(null, Collections.emptyList(), Collections.emptyList());

    public static class Action {
        public enum Type {
            INTAKE,
            LAUNCHER,
            FEED,
            WAIT
        }

        public final Type type;

        /** INTAKE: "in", "out" or "off". FEED: "pollen" or "nectar". */
        public final String mode;

        /** LAUNCHER: whether the launcher motors spin. */
        public final boolean enabled;

        /** LAUNCHER: hood angle in degrees. FEED and WAIT: duration in ms. */
        public final double value;

        Action(Type type, String mode, boolean enabled, double value) {
            this.type = type;
            this.mode = mode;
            this.enabled = enabled;
            this.value = value;
        }
    }

    public static class Step {
        public final double x;
        public final double y;
        public final double tolerance;
        public final List<Action> actions;

        public Step(double x, double y, double tolerance, List<Action> actions) {
            this.x = x;
            this.y = y;
            this.tolerance = tolerance;
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
                case "intake": {
                    String mode = action.getString("mode");
                    if (!mode.equals("in") && !mode.equals("out") && !mode.equals("off")) {
                        throw new JSONException(where + " has an unknown intake mode '" + mode + "'");
                    }
                    actions.add(new Action(Action.Type.INTAKE, mode, false, 0));
                    break;
                }
                case "launcher": {
                    double angle = action.getDouble("angle");
                    if (!(angle >= 0 && angle <= 180)) throw new JSONException(where + " hood angle is out of range");
                    actions.add(new Action(Action.Type.LAUNCHER, null, action.getBoolean("enabled"), angle));
                    break;
                }
                case "feed": {
                    String piece = action.getString("piece");
                    if (!piece.equals("pollen") && !piece.equals("nectar")) {
                        throw new JSONException(where + " has an unknown game piece '" + piece + "'");
                    }
                    actions.add(new Action(Action.Type.FEED, piece, false, duration(action, where)));
                    break;
                }
                case "wait":
                    actions.add(new Action(Action.Type.WAIT, null, false, duration(action, where)));
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
                case INTAKE:
                    json.put("type", "intake").put("mode", action.mode);
                    break;
                case LAUNCHER:
                    json.put("type", "launcher").put("enabled", action.enabled).put("angle", action.value);
                    break;
                case FEED:
                    json.put("type", "feed").put("piece", action.mode).put("duration", action.value);
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
