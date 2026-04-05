# Deploy Guide

## Docker Compose
在 deploy 目录执行：

```bash
docker compose up --build -d
```

访问地址：
- Frontend: http://localhost:3000
- Backend: http://localhost:8080
- MySQL: localhost:3306

## Stop
```bash
docker compose down
```

## Notes
- 首次启动会自动执行 ../database 下的 schema.sql 和 seed.sql。
- 上传文件保存在服务器目录：/home/PetDatabase（宿主机路径）。
