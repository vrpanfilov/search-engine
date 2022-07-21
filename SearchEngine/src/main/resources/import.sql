alter table `page` modify `content` mediumtext  character set utf8mb4 collate utf8mb4_unicode_ci;
alter table `page` add index `IX_page_path` (`path`(128) asc) visible;

insert into `field` (id, name, selector, weight) values (1, 'title', 'title', 1.0);
insert into `field` (id, name, selector, weight) values (2, 'body', 'body', 0.8);
insert into `field` (id, name, selector, weight) values (3, 'h1', 'h1', 0.1);
