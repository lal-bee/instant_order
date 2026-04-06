-- 测试数据：1董事长、4分店、每店1店长+2店员（单总部模型）

INSERT INTO store (id, name, manager_employee_id, status, create_user, update_user, create_time, update_time)
VALUES (1001, '广州天河分店', NULL, 1, 100, 100, NOW(), NOW());

INSERT INTO store (id, name, manager_employee_id, status, create_user, update_user, create_time, update_time)
VALUES (1002, '深圳南山分店', NULL, 1, 100, 100, NOW(), NOW());

INSERT INTO store (id, name, manager_employee_id, status, create_user, update_user, create_time, update_time)
VALUES (1003, '上海浦东分店', NULL, 1, 100, 100, NOW(), NOW());

INSERT INTO store (id, name, manager_employee_id, status, create_user, update_user, create_time, update_time)
VALUES (1004, '杭州西湖分店', NULL, 1, 100, 100, NOW(), NOW());

INSERT INTO employee (id, name, account, password, phone, age, gender, pic, store_id, role, status, create_user, update_user, create_time, update_time)
VALUES (9001, '董事长', 'chairman', MD5('123456'), '13900000001', 45, 1, NULL, 1001, 2, 1, 100, 100, NOW(), NOW());

INSERT INTO employee (id, name, account, password, phone, age, gender, pic, store_id, role, status, create_user, update_user, create_time, update_time)
VALUES
    (9101, '天河店长', 'mgr_tianhe', MD5('123456'), '13900000101', 32, 1, NULL, 1001, 1, 1, 9001, 9001, NOW(), NOW()),
    (9102, '南山店长', 'mgr_nanshan', MD5('123456'), '13900000102', 33, 1, NULL, 1002, 1, 1, 9001, 9001, NOW(), NOW()),
    (9103, '浦东店长', 'mgr_pudong', MD5('123456'), '13900000103', 34, 1, NULL, 1003, 1, 1, 9001, 9001, NOW(), NOW()),
    (9104, '西湖店长', 'mgr_xihu', MD5('123456'), '13900000104', 35, 1, NULL, 1004, 1, 1, 9001, 9001, NOW(), NOW());

UPDATE store
SET manager_employee_id = CASE id
    WHEN 1001 THEN 9101
    WHEN 1002 THEN 9102
    WHEN 1003 THEN 9103
    WHEN 1004 THEN 9104
END
WHERE id IN (1001, 1002, 1003, 1004);

INSERT INTO employee (id, name, account, password, phone, age, gender, pic, store_id, role, status, create_user, update_user, create_time, update_time)
VALUES
    (9201, '天河店员A', 'emp_tianhe_a', MD5('123456'), '13900000201', 24, 1, NULL, 1001, 0, 1, 9101, 9101, NOW(), NOW()),
    (9202, '天河店员B', 'emp_tianhe_b', MD5('123456'), '13900000202', 25, 0, NULL, 1001, 0, 1, 9101, 9101, NOW(), NOW());

INSERT INTO employee (id, name, account, password, phone, age, gender, pic, store_id, role, status, create_user, update_user, create_time, update_time)
VALUES
    (9203, '南山店员A', 'emp_nanshan_a', MD5('123456'), '13900000203', 26, 1, NULL, 1002, 0, 1, 9102, 9102, NOW(), NOW()),
    (9204, '南山店员B', 'emp_nanshan_b', MD5('123456'), '13900000204', 23, 0, NULL, 1002, 0, 1, 9102, 9102, NOW(), NOW());

INSERT INTO employee (id, name, account, password, phone, age, gender, pic, store_id, role, status, create_user, update_user, create_time, update_time)
VALUES
    (9205, '浦东店员A', 'emp_pudong_a', MD5('123456'), '13900000205', 27, 1, NULL, 1003, 0, 1, 9103, 9103, NOW(), NOW()),
    (9206, '浦东店员B', 'emp_pudong_b', MD5('123456'), '13900000206', 22, 0, NULL, 1003, 0, 1, 9103, 9103, NOW(), NOW());

INSERT INTO employee (id, name, account, password, phone, age, gender, pic, store_id, role, status, create_user, update_user, create_time, update_time)
VALUES
    (9207, '西湖店员A', 'emp_xihu_a', MD5('123456'), '13900000207', 28, 1, NULL, 1004, 0, 1, 9104, 9104, NOW(), NOW()),
    (9208, '西湖店员B', 'emp_xihu_b', MD5('123456'), '13900000208', 21, 0, NULL, 1004, 0, 1, 9104, 9104, NOW(), NOW());
