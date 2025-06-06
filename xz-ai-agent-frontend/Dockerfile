# 前端构建阶段
FROM node:20-alpine AS build
WORKDIR /app
# 直接执行此命令，会导致把node_modules等不需要打包的文件也打包进去，导致打包出来的镜像过大
# 故需要配合.dockerignore忽略不需要打包的文件
COPY . .
RUN npm install
RUN npm run build

# 运行阶段 - 使用 nginx 托管静态文件
FROM nginx:alpine
# 复制构建产物到 nginx 静态文件目录
COPY --from=build /app/dist /usr/share/nginx/html
# 复制自定义 nginx 配置替换默认配置
COPY nginx.conf /etc/nginx/conf.d/default.conf
# 暴露端口（Nginx默认使用80端口）
EXPOSE 80
# 启动 Nginx
CMD ["nginx", "-g", "daemon off;"]