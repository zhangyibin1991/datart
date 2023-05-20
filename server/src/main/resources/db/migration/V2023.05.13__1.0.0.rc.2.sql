CREATE TABLE `job` (
    `id` VARCHAR(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
    `name` VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
    `state` VARCHAR(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
    `sql` TEXT CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
    `parallelism` TINYINT UNSIGNED NOT NULL,
    `create_time` timestamp(0) NULL DEFAULT NULL,
    `create_by` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
    `update_time` timestamp(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0),
    `update_by` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    `org_id` int(11) NULL DEFAULT NULL,
    INDEX `index_id` (`id`) USING HASH
);