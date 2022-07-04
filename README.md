## Getting started

1. Run `docker compose up -d` to launch service with the database (PostgreSQL) instance locally.
2. If you wish to test APIs with _Postman_ APP:

- Run **InventiHomeworkApplication**

[![Run in Postman](https://run.pstmn.io/button.svg)](https://app.getpostman.com/run-collection/14940947-edbbe8ed-85fe-49c0-9a97-351825dd2f54?action=collection%2Ffork&collection-url=entityId%3D14940947-edbbe8ed-85fe-49c0-9a97-351825dd2f54%26entityType%3Dcollection%26workspaceId%3D372481e2-4deb-41e4-bf03-9fe61d59fee0)

Use [src/test/java/com/inventi/homework/postman-test-data.csv](src/test/java/com/inventi/homework/postman-test-data.csv)
file as "Body" value to import test data.

****NOTE** - if testing via _Postman_ - do it before running tests, located
in [src/test/java/com/inventi/homework](src/test/java/com/inventi/homework) so the account values, needed for _Postman_
testing are present in the database and not overridden by tests. You will be able to run tests **after** _Postman_
without any issues.