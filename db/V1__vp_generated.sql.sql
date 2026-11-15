CREATE TABLE addresses (
  address_id     bigint(20) NOT NULL AUTO_INCREMENT, 
  customer_id    bigint(20) NOT NULL, 
  receiver_name  varchar(100) NOT NULL, 
  receiver_phone varchar(15) NOT NULL, 
  address_line   varchar(255) NOT NULL, 
  is_default     tinyint(1) DEFAULT 0 NOT NULL, 
  PRIMARY KEY (address_id)) ENGINE=InnoDB;
CREATE TABLE customers (
  customer_id        bigint(20) NOT NULL AUTO_INCREMENT, 
  full_name          varchar(100) NOT NULL, 
  password_hash      varchar(255) NOT NULL, 
  failed_login_count tinyint(3) DEFAULT 0 NOT NULL, 
  locked_until       datetime NULL, 
  email              varchar(120) UNIQUE, 
  phone              varchar(15) UNIQUE, 
  PRIMARY KEY (customer_id)) ENGINE=InnoDB;
CREATE TABLE carts (
  cart_id     bigint(20) NOT NULL AUTO_INCREMENT, 
  customer_id bigint(20) NOT NULL UNIQUE, 
  updated_at  datetime NOT NULL, 
  PRIMARY KEY (cart_id)) ENGINE=InnoDB;
CREATE TABLE orders (
  order_id         bigint(20) NOT NULL AUTO_INCREMENT, 
  customer_id      bigint(20) NOT NULL, 
  order_code       varchar(20) NOT NULL UNIQUE, 
  order_date       datetime NOT NULL, 
  receiver_name    varchar(100) NOT NULL, 
  receiver_phone   varchar(15) NOT NULL, 
  shipping_address varchar(255) NOT NULL, 
  subtotal         decimal(12, 0) NOT NULL, 
  shipping_fee     decimal(12, 0) NOT NULL, 
  total            decimal(12, 0) NOT NULL, 
  payment_method   ENUM('COD','ONLINE') NOT NULL, 
  status           ENUM('CHO_THANH_TOAN','CHO_XAC_NHAN','DANG_GIAO','DA_GIAO','DA_HUY') NOT NULL, 
  expires_at       datetime NULL, 
  PRIMARY KEY (order_id)) ENGINE=InnoDB;
CREATE TABLE payments (
  payment_id bigint(20) NOT NULL AUTO_INCREMENT, 
  order_id   bigint(20) NOT NULL, 
  method     varchar(10) NOT NULL, 
  amount     decimal(12, 0) NOT NULL, 
  status     ENUM('DANG_XU_LY','THANH_CONG','THAT_BAI','SAI_LECH') NOT NULL, 
  txn_ref    varchar(64), 
  paid_at    datetime NULL, 
  PRIMARY KEY (payment_id)) ENGINE=InnoDB;
CREATE TABLE order_status_histories (
  history_id  bigint(20) NOT NULL AUTO_INCREMENT, 
  staff_id    bigint(20), 
  order_id    bigint(20) NOT NULL, 
  from_status varchar(20), 
  to_status   varchar(20) NOT NULL, 
  note        varchar(255), 
  changed_at  datetime NOT NULL, 
  PRIMARY KEY (history_id)) ENGINE=InnoDB;
CREATE TABLE staff (
  staff_id           bigint(20) NOT NULL AUTO_INCREMENT, 
  full_name          varchar(100) NOT NULL, 
  password_hash      varchar(255) NOT NULL, 
  failed_login_count tinyint(3) DEFAULT 0 NOT NULL, 
  locked_until       datetime NULL, 
  username           varchar(50) NOT NULL UNIQUE, 
  role               varchar(20) NOT NULL, 
  PRIMARY KEY (staff_id)) ENGINE=InnoDB;
CREATE TABLE order_items (
  product_name varchar(150) NOT NULL, 
  variant_id   bigint(20) NOT NULL, 
  order_id     bigint(20) NOT NULL, 
  `size`       varchar(5) NOT NULL, 
  color        varchar(30) NOT NULL, 
  unit_price   decimal(12, 0) NOT NULL CHECK (unit_price > 0), 
  quantity     tinyint(3) NOT NULL CHECK (quantity > 0), 
  PRIMARY KEY (variant_id, 
  order_id)) ENGINE=InnoDB;
CREATE TABLE product_variants (
  variant_id bigint(20) NOT NULL AUTO_INCREMENT, 
  product_id bigint(20) NOT NULL, 
  sku        varchar(40) NOT NULL UNIQUE, 
  `size`     varchar(5) NOT NULL, 
  color      varchar(30) NOT NULL, 
  stock_qty  int(10) DEFAULT 0 NOT NULL CHECK (stock_qty >= 0), 
  PRIMARY KEY (variant_id), 
  CONSTRAINT uq_variant_product_size_color 
    UNIQUE (product_id, `size`, color)) ENGINE=InnoDB;
CREATE TABLE cart_items (
  quantity   tinyint(3) NOT NULL CHECK (quantity BETWEEN 1 AND 10), 
  variant_id bigint(20) NOT NULL, 
  cart_id    bigint(20) NOT NULL, 
  added_at   datetime NOT NULL, 
  PRIMARY KEY (variant_id, 
  cart_id)) ENGINE=InnoDB;
CREATE TABLE products (
  product_id  bigint(20) NOT NULL AUTO_INCREMENT, 
  category_id bigint(20) NOT NULL, 
  name        varchar(150) NOT NULL, 
  description text, 
  material    varchar(80), 
  price       decimal(12, 0) NOT NULL CHECK (price > 0), 
  is_visible  tinyint(1) DEFAULT 1 NOT NULL, 
  fit_type    ENUM('SLIM','REGULAR','OVERSIZE') NOT NULL, 
  PRIMARY KEY (product_id)) ENGINE=InnoDB;
CREATE TABLE categories (
  category_id bigint(20) NOT NULL AUTO_INCREMENT, 
  name        varchar(80) NOT NULL, 
  slug        varchar(80) NOT NULL UNIQUE, 
  PRIMARY KEY (category_id)) ENGINE=InnoDB;
CREATE TABLE product_images (
  image_id   bigint(20) NOT NULL AUTO_INCREMENT, 
  product_id bigint(20) NOT NULL, 
  url        varchar(255) NOT NULL, 
  is_main    tinyint(1) DEFAULT 0 NOT NULL, 
  PRIMARY KEY (image_id)) ENGINE=InnoDB;
CREATE TABLE product_change_logs (
  log_id     bigint(20) NOT NULL AUTO_INCREMENT, 
  product_id bigint(20) NOT NULL, 
  staff_id   bigint(20) NOT NULL, 
  variant_id bigint(20), 
  field_name varchar(30) NOT NULL, 
  old_value  varchar(255), 
  new_value  varchar(255), 
  changed_at datetime NOT NULL, 
  PRIMARY KEY (log_id)) ENGINE=InnoDB;
CREATE TABLE body_profiles (
  height_cm   smallint(6) NOT NULL, 
  customer_id bigint(20) NOT NULL, 
  weight_kg   smallint(6) NOT NULL, 
  fit_pref    ENUM('OM','VUA','RONG') NOT NULL, 
  consent_at  datetime NOT NULL, 
  PRIMARY KEY (customer_id)) ENGINE=InnoDB;
CREATE TABLE size_charts (
  chart_id   bigint(20) NOT NULL AUTO_INCREMENT, 
  product_id bigint(20) NOT NULL, 
  `size`     varchar(5) NOT NULL, 
  height_min smallint(6) NOT NULL, 
  height_max smallint(6) NOT NULL, 
  weight_min smallint(6) NOT NULL, 
  weight_max smallint(6) NOT NULL, 
  PRIMARY KEY (chart_id)) ENGINE=InnoDB;
CREATE TABLE return_records (
  return_id  bigint(20) NOT NULL AUTO_INCREMENT, 
  created_by bigint(20) NOT NULL, 
  reason     ENUM('SIZE_LON','SIZE_NHO','KHAC') NOT NULL, 
  created_at datetime NOT NULL, 
  variant_id bigint(20) NOT NULL, 
  order_id   bigint(20) NOT NULL, 
  PRIMARY KEY (return_id)) ENGINE=InnoDB;
ALTER TABLE payments ADD CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders (order_id);
ALTER TABLE order_items ADD CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (order_id) ON DELETE Cascade;
ALTER TABLE product_variants ADD CONSTRAINT fk_variants_product FOREIGN KEY (product_id) REFERENCES products (product_id) ON DELETE Cascade;
ALTER TABLE product_change_logs ADD CONSTRAINT fk_change_logs_variant FOREIGN KEY (variant_id) REFERENCES product_variants (variant_id);
ALTER TABLE orders ADD CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers (customer_id);
ALTER TABLE addresses ADD CONSTRAINT fk_addresses_customer FOREIGN KEY (customer_id) REFERENCES customers (customer_id) ON DELETE Cascade;
ALTER TABLE carts ADD CONSTRAINT fk_carts_customer FOREIGN KEY (customer_id) REFERENCES customers (customer_id);
ALTER TABLE cart_items ADD CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts (cart_id) ON DELETE Cascade;
ALTER TABLE order_status_histories ADD CONSTRAINT fk_status_histories_order FOREIGN KEY (order_id) REFERENCES orders (order_id) ON DELETE Cascade;
ALTER TABLE order_status_histories ADD CONSTRAINT fk_status_histories_staff FOREIGN KEY (staff_id) REFERENCES staff (staff_id);
ALTER TABLE product_change_logs ADD CONSTRAINT fk_change_logs_staff FOREIGN KEY (staff_id) REFERENCES staff (staff_id);
ALTER TABLE order_items ADD CONSTRAINT fk_order_items_variant FOREIGN KEY (variant_id) REFERENCES product_variants (variant_id);
ALTER TABLE cart_items ADD CONSTRAINT fk_cart_items_variant FOREIGN KEY (variant_id) REFERENCES product_variants (variant_id);
ALTER TABLE products ADD CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (category_id);
ALTER TABLE product_images ADD CONSTRAINT fk_product_images_product FOREIGN KEY (product_id) REFERENCES products (product_id) ON DELETE Cascade;
ALTER TABLE product_change_logs ADD CONSTRAINT fk_change_logs_product FOREIGN KEY (product_id) REFERENCES products (product_id);
ALTER TABLE body_profiles ADD CONSTRAINT fk_body_profiles_customer FOREIGN KEY (customer_id) REFERENCES customers (customer_id) ON DELETE Cascade;
ALTER TABLE size_charts ADD CONSTRAINT fk_size_charts_product FOREIGN KEY (product_id) REFERENCES products (product_id) ON DELETE Cascade;
ALTER TABLE return_records ADD CONSTRAINT fk_returns_order_item FOREIGN KEY (variant_id, order_id) REFERENCES order_items (variant_id, order_id);
ALTER TABLE return_records ADD CONSTRAINT fk_returns_created_by FOREIGN KEY (created_by) REFERENCES staff (staff_id);
