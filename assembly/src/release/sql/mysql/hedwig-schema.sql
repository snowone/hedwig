SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

DROP SCHEMA IF EXISTS hedwig;
CREATE SCHEMA hedwig;
USE hedwig;

-- -----------------------------------------------------
-- Table `alias`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `alias` ;

CREATE  TABLE IF NOT EXISTS `alias` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `alias` VARCHAR(100) NOT NULL ,
  `deliver_to` BIGINT UNSIGNED NOT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB;

CREATE INDEX `fk_alias_user1` ON `alias` (`deliver_to` ASC) ;

-- -----------------------------------------------------
-- Table `user`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `user` ;

CREATE  TABLE IF NOT EXISTS `user` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `userid` VARCHAR(100) NOT NULL ,
  `passwd` VARCHAR(34) NOT NULL ,
  `name` VARCHAR(60) NULL ,
  `maxmail_size` BIGINT UNSIGNED NOT NULL DEFAULT '0',
  `forward` VARCHAR(100) NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB DEFAULT CHARSET=utf8;

CREATE UNIQUE INDEX `uk_user_userid` ON `user` (`userid` ASC) ;

-- -----------------------------------------------------
-- Table `mailbox`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `mailbox` ;

CREATE  TABLE IF NOT EXISTS `mailbox` (
  `mailboxid` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `ownerid` BIGINT UNSIGNED NOT NULL ,
  `name` VARCHAR(255) NOT NULL ,
  `noinferiors` CHAR NOT NULL DEFAULT 'N' ,
  `noselect` CHAR NOT NULL DEFAULT 'N' ,
  `readonly` CHAR NOT NULL DEFAULT 'N' ,
  `nextuid` BIGINT UNSIGNED NOT NULL ,
  `uidvalidity` BIGINT UNSIGNED NOT NULL ,
  PRIMARY KEY (`mailboxid`) )
ENGINE = InnoDB DEFAULT CHARSET=utf8;

CREATE INDEX `fk_mailbox_user` ON `mailbox` (`ownerid` ASC) ;

-- -----------------------------------------------------
-- Table `subscription`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `subscription` ;

CREATE  TABLE IF NOT EXISTS `subscription` (
  `mailboxid` BIGINT UNSIGNED NOT NULL ,
  `userid` BIGINT UNSIGNED NOT NULL ,
  `name` VARCHAR(255) NOT NULL )
ENGINE = InnoDB DEFAULT CHARSET=utf8;

CREATE UNIQUE INDEX `uk_subscription` ON `subscription` (`userid` ASC, `name` ASC) ;

-- -----------------------------------------------------
-- Table `physmessage`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `physmessage` ;

CREATE  TABLE IF NOT EXISTS `physmessage` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `size` BIGINT UNSIGNED NOT NULL ,
  `internaldate` DATETIME NOT NULL ,
  `subject` VARCHAR(500) NULL ,
  `sentdate` DATETIME NULL ,
  `fromaddr` VARCHAR(100) NULL DEFAULT '' ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB DEFAULT CHARSET=utf8;


-- -----------------------------------------------------
-- Table `message`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `message` ;

CREATE  TABLE IF NOT EXISTS `message` (
  `messageid` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `mailboxid` BIGINT UNSIGNED NOT NULL ,
  `physmessageid` BIGINT UNSIGNED NOT NULL ,
  `seen` CHAR NOT NULL DEFAULT 'N' ,
  `answered` CHAR NOT NULL DEFAULT 'N' ,
  `deleted` CHAR NOT NULL DEFAULT 'N' ,
  `flagged` CHAR NOT NULL DEFAULT 'N' ,
  `recent` CHAR NOT NULL DEFAULT 'Y' ,
  `draft` CHAR NOT NULL DEFAULT 'N' ,
  PRIMARY KEY (`messageid`) )
ENGINE = InnoDB DEFAULT CHARSET=utf8;

CREATE INDEX `fk_message_mailbox1` ON `message` (`mailboxid` ASC) ;

CREATE INDEX `fk_message_physmessage1` ON `message` (`physmessageid` ASC) ;


-- -----------------------------------------------------
-- Table `headername`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `headername` ;

CREATE  TABLE IF NOT EXISTS `headername` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `headername` VARCHAR(100) NOT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB DEFAULT CHARSET=utf8;

CREATE UNIQUE INDEX `uk_headername` ON `headername` (`headername` ASC) ;

-- -----------------------------------------------------
-- Table `headervalue`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `headervalue` ;

CREATE  TABLE IF NOT EXISTS `headervalue` (
  `id` BIGINT NOT NULL AUTO_INCREMENT ,
  `physmessageid` BIGINT UNSIGNED NOT NULL ,
  `headernameid` BIGINT UNSIGNED NOT NULL ,
  `headervalue` TEXT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB DEFAULT CHARSET=utf8;

CREATE INDEX `fk_headervalue_physmessage1` ON `headervalue` (`physmessageid` ASC) ;

CREATE INDEX `fk_headervalue_headername1` ON `headervalue` (`headernameid` ASC) ;


-- -----------------------------------------------------
-- Table `acl`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `acl` ;

CREATE  TABLE IF NOT EXISTS `acl` (
  `user_id` BIGINT UNSIGNED NOT NULL ,
  `mailboxid` BIGINT UNSIGNED NOT NULL ,
  `lookup` CHAR NOT NULL DEFAULT 'N' ,
  `read` CHAR NOT NULL DEFAULT 'N' ,
  `seen` CHAR NOT NULL DEFAULT 'N' ,
  `write` CHAR NOT NULL DEFAULT 'N' ,
  `insert` CHAR NOT NULL DEFAULT 'N' ,
  `post` CHAR NOT NULL DEFAULT 'N' ,
  `create` CHAR NOT NULL DEFAULT 'N' ,
  `delete` CHAR NOT NULL DEFAULT 'N' ,
  `deletemsg` CHAR NOT NULL DEFAULT 'N' ,
  `expunge` CHAR NOT NULL DEFAULT 'N' ,
  `admin` CHAR NOT NULL DEFAULT 'N' ,
  PRIMARY KEY (`user_id`, `mailboxid`) )
ENGINE = InnoDB;

CREATE INDEX `fk_acl_user1` ON `acl` (`user_id` ASC) ;

CREATE INDEX `fk_acl_mailbox1` ON `acl` (`mailboxid` ASC) ;


-- -----------------------------------------------------
-- Table `keyword`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `keyword` ;

CREATE  TABLE IF NOT EXISTS `keyword` (
  `messageid` BIGINT UNSIGNED NOT NULL ,
  `keyword` VARCHAR(255) NOT NULL )
ENGINE = InnoDB;

CREATE INDEX `fk_keyword_message1` ON `keyword` (`messageid` ASC) ;


DELIMITER //

DROP TRIGGER IF EXISTS `ins_message` //
CREATE TRIGGER ins_message AFTER INSERT ON message FOR EACH ROW BEGIN
    UPDATE mailbox SET nextuid=new.messageid+1 WHERE mailboxid=new.mailboxid;
END
//


DELIMITER ;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
