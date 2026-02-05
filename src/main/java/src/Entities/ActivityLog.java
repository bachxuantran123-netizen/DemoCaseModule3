package src.Entities;

import java.sql.Timestamp;

public class ActivityLog {
    private int log_id;
    private String username;
    private String action;
    private Timestamp logTime;

    public ActivityLog(int log_id, String username, String action, Timestamp logTime) {
        this.log_id = log_id;
        this.username = username;
        this.action = action;
        this.logTime = logTime;
    }

    public int getId() {
        return log_id;
    }
    public String getUsername() {
        return username;
    }
    public String getAction() {
        return action;
    }
    public Timestamp getLogTime() {
        return logTime;
    }
}