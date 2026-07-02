package org.quartz.impl.jdbcjobstore;

/**
 * @Description: Quartz JDBC 存储常量备份接口
 */
public interface ConstantsBak {
    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     *
     * Constants.
     *
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    // Table names
    String TABLE_JOB_DETAILS = "job_details";

    String TABLE_TRIGGERS = "triggers";

    String TABLE_SIMPLE_TRIGGERS = "simple_triggers";

    String TABLE_CRON_TRIGGERS = "cron_triggers";

    String TABLE_BLOB_TRIGGERS = "blob_triggers";

    String TABLE_FIRED_TRIGGERS = "fired_triggers";

    String TABLE_CALENDARS = "calendars";

    String TABLE_PAUSED_TRIGGERS = "paused_trigger_grps";

    String TABLE_LOCKS = "locks";

    String TABLE_SCHEDULER_STATE = "scheduler_state";

    // TABLE_JOB_DETAILS columns names

    String COL_SCHEDULER_NAME = "sched_name";

    String COL_JOB_NAME = "job_name";

    String COL_JOB_GROUP = "job_group";

    String COL_IS_DURABLE = "is_durable";

    String COL_IS_VOLATILE = "is_volatile";

    String COL_IS_NONCONCURRENT = "is_nonconcurrent";

    String COL_IS_UPDATE_DATA = "is_update_data";

    String COL_REQUESTS_RECOVERY = "requests_recovery";

    String COL_JOB_DATAMAP = "job_data";

    String COL_JOB_CLASS = "job_class_name";

    String COL_DESCRIPTION = "description";

    // TABLE_TRIGGERS columns names
    String COL_TRIGGER_NAME = "trigger_name";

    String COL_TRIGGER_GROUP = "trigger_group";

    String COL_NEXT_FIRE_TIME = "next_fire_time";

    String COL_PREV_FIRE_TIME = "prev_fire_time";

    String COL_TRIGGER_STATE = "trigger_state";

    String COL_TRIGGER_TYPE = "trigger_type";

    String COL_START_TIME = "start_time";

    String COL_END_TIME = "end_time";

    String COL_PRIORITY = "priority";

    String COL_MISFIRE_INSTRUCTION = "misfire_instr";

    String ALIAS_COL_NEXT_FIRE_TIME = "alias_nxt_fr_tm";

    // TABLE_SIMPLE_TRIGGERS columns names
    String COL_REPEAT_COUNT = "repeat_count";

    String COL_REPEAT_INTERVAL = "repeat_interval";

    String COL_TIMES_TRIGGERED = "times_triggered";

    // TABLE_CRON_TRIGGERS columns names
    String COL_CRON_EXPRESSION = "cron_expression";

    // TABLE_BLOB_TRIGGERS columns names
    String COL_BLOB = "blob_data";

    String COL_TIME_ZONE_ID = "time_zone_id";

    // TABLE_FIRED_TRIGGERS columns names
    String COL_INSTANCE_NAME = "instance_name";

    String COL_FIRED_TIME = "fired_time";

    String COL_SCHED_TIME = "sched_time";

    String COL_ENTRY_ID = "entry_id";

    String COL_ENTRY_STATE = "state";

    // TABLE_CALENDARS columns names
    String COL_CALENDAR_NAME = "calendar_name";

    String COL_CALENDAR = "calendar";

    // TABLE_LOCKS columns names
    String COL_LOCK_NAME = "lock_name";

    // TABLE_LOCKS columns names
    String COL_LAST_CHECKIN_TIME = "last_checkin_time";

    String COL_CHECKIN_INTERVAL = "checkin_interval";

    // MISC CONSTANTS
    String DEFAULT_TABLE_PREFIX = "qrtz_";

    // STATES
    String STATE_WAITING = "waiting";

    String STATE_ACQUIRED = "acquired";

    String STATE_EXECUTING = "executing";

    String STATE_COMPLETE = "complete";

    String STATE_BLOCKED = "blocked";

    String STATE_ERROR = "error";

    String STATE_PAUSED = "paused";

    String STATE_PAUSED_BLOCKED = "paused_blocked";

    String STATE_DELETED = "deleted";

    /**
     * @deprecated Whether a trigger has misfired is no longer a state, but
     * rather now identified dynamically by whether the trigger's next fire
     * time is more than the misfire threshold time in the past.
     */
    String STATE_MISFIRED = "misfired";

    String ALL_GROUPS_PAUSED = "_$_all_groups_paused_$_";

    // TRIGGER TYPES
    /**
     * Simple Trigger type.
     */
    String TTYPE_SIMPLE = "simple";

    /**
     * Cron Trigger type.
     */
    String TTYPE_CRON = "cron";

    /**
     * Calendar Interval Trigger type.
     */
    String TTYPE_CAL_INT = "cal_int";

    /**
     * Daily Time Interval Trigger type.
     */
    String TTYPE_DAILY_TIME_INT = "daily_i";

    /**
     * A general blob Trigger type.
     */
    String TTYPE_BLOB = "blob";
}
