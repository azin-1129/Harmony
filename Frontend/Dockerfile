FROM node:latest AS build-stage
WORKDIR /app

# 의존성 설치
COPY package*.json ./
RUN npm install
# 소스 복사, 빌드
COPY . .
RUN npm run build

# nginx로 정적 파일 서빙
FROM nginx:trixie
# nginx 설정 파일 복사
COPY nginx.conf /etc/nginx/conf.d/default.conf
# 빌드된 파일을 nginx의 html 디렉토리로 복사
COPY --from=build-stage /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]