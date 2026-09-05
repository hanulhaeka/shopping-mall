insert into users (login_id, name, hashed_password, role, status, created_at, phone_number)
values ('user', 'user', '$2a$12$OAQnrsWb9r3a7H9BtS4G1ez66zw2poNwXSdjnQrHX4TWiae0EJafW', 'USER', 'ACTIVE', '2026-06-01 14:30:00', '010-1234-5678');

insert into users (login_id, name, hashed_password, role, status, created_at)
values ('admin', 'admin', '$2a$12$OAQnrsWb9r3a7H9BtS4G1ez66zw2poNwXSdjnQrHX4TWiae0EJafW', 'ADMIN', 'ACTIVE', '2026-06-01 14:30:00');

insert into products (name, price, stock, status, image_head_url, image1_url, image2_url, image3_url, created_at)
values ('달력', 15000, 10, 'ACTIVE', 'calendar_head.png', 'calendar_detail1.png', 'calendar_detail2.png', null, '2026-06-01 14:30:00');

insert into products (name, price, stock, status, image_head_url, image1_url, image2_url, image3_url, created_at)
values ('의자', 50000, 50, 'ACTIVE', 'chair_head.png', 'chair_detail1.png', 'chair_detail2.png', null, '2026-06-01 14:30:00');

insert into products (name, price, stock, status, image_head_url, image1_url, image2_url, image3_url, created_at)
values ('암막 커튼', 30000, 30, 'ACTIVE', 'curtain_head.png', 'curtain_detail1.png', null, null, '2026-06-01 14:30:00');

insert into products (name, price, stock, status, image_head_url, image1_url, image2_url, image3_url, created_at)
values ('스마트 안경', 100000, 10, 'ACTIVE', 'glasses_head.png', 'glasses_detail1.png', 'glasses_detail2.png', null, '2026-06-01 14:30:00');

insert into products (name, price, stock, status, image_head_url, image1_url, image2_url, image3_url, created_at)
values ('모니터', 150000, 15, 'ACTIVE', 'monitor_head.png', 'monitor_detail1.png', null, null, '2026-06-01 14:30:00');

insert into products (name, price, stock, status, image_head_url, image1_url, image2_url, image3_url, created_at)
values ('네이처 하모니', 3000, 30, 'ACTIVE', 'nature_harmony_head.png', 'nature_harmony_detail1.png', 'nature_harmony_detail2.png', 'nature_harmony_detail3.png', '2026-06-01 14:30:00');

insert into products (name, price, stock, status, image_head_url, image1_url, image2_url, image3_url, created_at)
values ('고급 샤프', 50000, 50, 'ACTIVE', 'pencil_head.png', 'pencil_detail1.png', 'pencil_detail2.png', 'pencil_detail3.png', '2026-06-01 14:30:00');

insert into products (name, price, stock, status, image_head_url, image1_url, image2_url, image3_url, created_at)
values ('스마트 냄비', 200000, 20, 'ACTIVE', 'pot_head.png', 'pot_detail1.png', 'pot_detail2.png', 'pot_detail3.png', '2026-06-01 14:30:00');

insert into products (name, price, stock, status, image_head_url, image1_url, image2_url, image3_url, created_at)
values ('블루투스 스피커', 100000, 10, 'ACTIVE', 'speaker_head.png', 'speaker_detail1.png', 'speaker_detail2.png', 'speaker_detail3.png', '2026-06-01 14:30:00');

insert into products (name, price, stock, status, image_head_url, image1_url, image2_url, image3_url, created_at)
values ('벽 시계', 900000, 90, 'ACTIVE', 'wall_clock_head.png', 'wall_clock_detail1.png', 'wall_clock_detail2.png', 'wall_clock_detail3.png', '2026-06-01 14:30:00');