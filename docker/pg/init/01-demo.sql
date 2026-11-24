-- PostgreSQL 演示数据：用于本地验证数据源接入、关系导入、关联查询与 ER 图
-- 默认 schema 为 public，另建 sales schema 用于验证「指定 schema」场景

CREATE SCHEMA IF NOT EXISTS sales;

CREATE TABLE IF NOT EXISTS public.customers (
    id bigserial PRIMARY KEY,
    name varchar(64) NOT NULL,
    active boolean NOT NULL DEFAULT true,
    created_at timestamptz NOT NULL DEFAULT now(),
    tags text[],
    meta jsonb,
    balance numeric(12,2)
);
COMMENT ON TABLE public.customers IS '客户';
COMMENT ON COLUMN public.customers.id IS '客户ID';
COMMENT ON COLUMN public.customers.name IS '客户名称';
COMMENT ON COLUMN public.customers.active IS '是否启用';

CREATE TABLE IF NOT EXISTS public.orders (
    id bigserial PRIMARY KEY,
    customer_id bigint NOT NULL REFERENCES public.customers(id),
    status varchar(16) DEFAULT 'NEW',
    amount numeric(12,2),
    created_at timestamp
);
COMMENT ON TABLE public.orders IS '订单';
COMMENT ON COLUMN public.orders.customer_id IS '客户ID';
COMMENT ON COLUMN public.orders.status IS '订单状态';

CREATE TABLE IF NOT EXISTS public.order_items (
    order_id bigint NOT NULL,
    line_no int NOT NULL,
    sku varchar(32) NOT NULL,
    qty int NOT NULL DEFAULT 1,
    PRIMARY KEY (order_id, line_no),
    CONSTRAINT fk_items_order FOREIGN KEY (order_id) REFERENCES public.orders(id)
);
COMMENT ON TABLE public.order_items IS '订单明细';
COMMENT ON COLUMN public.order_items.sku IS '商品编码';

CREATE TABLE IF NOT EXISTS sales.products (
    id bigserial PRIMARY KEY,
    name varchar(64) NOT NULL,
    price numeric(12,2) NOT NULL DEFAULT 0,
    attributes jsonb,
    created_at timestamptz DEFAULT now()
);
COMMENT ON TABLE sales.products IS '商品';
COMMENT ON COLUMN sales.products.name IS '商品名称';
COMMENT ON COLUMN sales.products.price IS '价格';

-- 跨 schema 外键：验证不会被错误导入
CREATE TABLE IF NOT EXISTS sales.order_snapshot (
    snapshot_id bigserial PRIMARY KEY,
    order_id bigint NOT NULL REFERENCES public.orders(id),
    snapshot_at timestamptz NOT NULL DEFAULT now()
);
COMMENT ON TABLE sales.order_snapshot IS '订单快照';

INSERT INTO public.customers(name, active, balance) VALUES
    ('张三', true, 100.50),
    ('李四', false, 0);
INSERT INTO public.orders(customer_id, status, amount, created_at) VALUES
    (1, 'PAID', 99.99, now()),
    (2, 'NEW', 10, now());
INSERT INTO public.order_items(order_id, line_no, sku, qty) VALUES
    (1, 1, 'SKU-1', 1),
    (1, 2, 'SKU-2', 3);
INSERT INTO sales.products(name, price, attributes) VALUES
    ('键盘', 299.00, '{"color": "black"}'),
    ('鼠标', 99.00, '{"color": "white"}');
