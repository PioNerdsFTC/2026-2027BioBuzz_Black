package org.pionerds.ftc.teamcode.Driver;

public enum DriverActions {
    ENABLE_AIMBOT,
    DISABLE_AIMBOT,
    LAUNCH,
    ENABLE_INTAKE,
    DISABLE_INTAKE;


    public String toString(){
        String string = this.name();
        string = string.toLowerCase();
        while(string.contains("_")){
            string = string.substring(0,string.indexOf("_")) + "-" +string.substring(string.indexOf("_") + 1);
        }

        return string;
    }

}
