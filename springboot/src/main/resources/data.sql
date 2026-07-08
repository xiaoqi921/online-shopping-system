INSERT IGNORE INTO users (id, username, password, role) VALUES (1, 'admin', 'admin123', 'admin');
INSERT IGNORE INTO users (id, username, password, role) VALUES (2, 'user1', 'user123', 'user');

INSERT IGNORE INTO products (id, name, description, price, stock, image_url, category) VALUES (1, '高级无线降噪耳机', '头戴式主动降噪耳机，支持30小时超长续航，佩戴舒适，音质出色。', 299.99, 50, 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&q=80', '电子产品');
INSERT IGNORE INTO products (id, name, description, price, stock, image_url, category) VALUES (2, '机械游戏键盘', 'RGB背光机械键盘，段落式触感轴体，打字手感出色，适合游戏与办公。', 129.50, 30, 'https://images.unsplash.com/photo-1511512578047-dfb367046420?w=500&q=80', '电子产品');
INSERT IGNORE INTO products (id, name, description, price, stock, image_url, category) VALUES (3, '4K超高清显示器', '27英寸4K分辨率显示器，支持HDR，超窄边框设计，色彩细腻真实。', 349.00, 15, 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=500&q=80', '电子产品');
INSERT IGNORE INTO products (id, name, description, price, stock, image_url, category) VALUES (4, '人体工学办公椅', '可调节腰部支撑，透气网背设计，长时间久坐也不易疲劳，提升办公舒适度。', 199.99, 20, 'https://images.unsplash.com/photo-1505843490538-5133c6c7d0e1?w=500&q=80', '家居办公');
