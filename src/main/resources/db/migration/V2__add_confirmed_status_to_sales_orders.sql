-- Allow CONFIRMED status in sales_orders (constraint was created by Hibernate with old enum values)
ALTER TABLE sales_orders DROP CONSTRAINT IF EXISTS sales_orders_status_check;
ALTER TABLE sales_orders ADD CONSTRAINT sales_orders_status_check
    CHECK (status IN (
        'CREATED', 'CONFIRMED', 'RESERVED', 'PARTIAL_RESERVED',
        'SHIPPED', 'DELIVERED', 'CANCELED', 'APPROVED', 'RECEIVED'
    ));
