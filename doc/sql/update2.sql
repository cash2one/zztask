
ALTER TABLE `job_status` ADD COLUMN `node_key` VARCHAR(45) NULL DEFAULT ''  AFTER `gmt_modified` ;

CREATE  TABLE IF NOT EXISTS `job_node_running` (
      `id` INT(20) NOT NULL AUTO_INCREMENT ,
      `node_key` VARCHAR(45) NOT NULL DEFAULT '' COMMENT '节点Key' ,
      `job_id` VARCHAR(45) NOT NULL DEFAULT '' COMMENT '运行中的任务ID' ,
      `gmt_created` DATETIME NULL DEFAULT NULL ,
      `gmt_modified` DATETIME NULL DEFAULT NULL ,
      PRIMARY KEY (`id`) ,
      INDEX `idx_node_key` (`node_key` ASC) ,
      INDEX `idx_job_id` (`job_id` ASC) )
    ENGINE = MyISAM
    DEFAULT CHARACTER SET = utf8
    COLLATE = utf8_general_ci
    COMMENT = '节点运行中的任务';

    CREATE  TABLE IF NOT EXISTS `job_node` (
          `id` INT(20) NOT NULL AUTO_INCREMENT ,
          `node_key` VARCHAR(45) NOT NULL DEFAULT '' COMMENT '节点Key' ,
          `remark` VARCHAR(45) NULL DEFAULT '' COMMENT '备注' ,
          `status` VARCHAR(10) NOT NULL DEFAULT 'offline' COMMENT '节点状态（有些情况下，节点可能会被移除失效）\nonline\noffline\ndisuse' ,
          `gmt_created` DATETIME NULL DEFAULT NULL ,
          `gmt_modified` DATETIME NULL DEFAULT NULL ,
          PRIMARY KEY (`id`) ,
          INDEX `idx_node_key` (`node_key` ASC) )
        ENGINE = MyISAM
        DEFAULT CHARACTER SET = utf8
        COLLATE = utf8_general_ci
        COMMENT = '注册的节点信息';
