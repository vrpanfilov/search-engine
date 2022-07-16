alter table `page` modify `content` mediumtext  character set utf8mb4 collate utf8mb4_unicode_ci;

insert into `field` (id, name, selector, weight) values (1, 'title', 'title', 1.0);
insert into `field` (id, name, selector, weight) values (2, 'body', 'body', 0.8);
