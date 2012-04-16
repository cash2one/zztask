drop TABLE if exists TABLE `ast`.`bbs_statistic`;

CREATE  TABLE `ast`.`bbs_statistic` (
  `statistic_type` TINYINT(2) NOT NULL COMMENT '统计类型：1 本周牛帖，\n2 本周牛人，3 最牛网商' ,
  `pid` INT(20) NOT NULL ,
  `display_info` VARCHAR(45) NULL COMMENT '页面显示的信息［帖子标题，牛人ID，网商ID］' ,
  `statistic_column` VARCHAR(45) NULL COMMENT '参与统计的字段，有多个时以［，］分隔' ,
  `statistic_score` INT NULL COMMENT '统计排名得分' ,
  PRIMARY KEY (`statistic_type`, `pid`) )
ENGINE = MyISAM
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_general_ci;
