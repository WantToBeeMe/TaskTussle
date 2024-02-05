package me.wanttobee.tasktussle.generic.tasks

enum class TaskState(val isCompleted: Boolean) {
    ACTIVE(false),
    COMPLETED(true),
    COMPLETED_BY(true),
    HIDDEN(false),
    LOCKED(false),
    FAILED(false);
}