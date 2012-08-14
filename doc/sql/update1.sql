ALTER TABLE `job_definition` 
CHANGE COLUMN `job_group` `job_group` VARCHAR(45) NULL DEFAULT 'task' COMMENT '任务组\ntask:普通任务\nidx_task:索引任务'  , 
CHANGE COLUMN `end_time` `end_time` VARCHAR(45) NULL DEFAULT NULL COMMENT '任务执行的结束时段\n最后一次任务执行时间'  ;

update job_definition
set job_group = 'task'
where job_group is null;