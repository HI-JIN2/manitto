# 🚀 Manitto Backend 배포 가이드 (NCP + Docker)

## 📋 사전 준비

### 1. NCP 서버 생성
- **OS**: Ubuntu 22.04 LTS 권장
- **스펙**: 최소 2vCPU, 4GB RAM
- **포트 오픈 (ACG)**: 8080

### 2. Docker 설치

```bash
# Docker 설치
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Docker Compose 설치
sudo apt-get update
sudo apt-get install docker-compose-plugin

# 현재 사용자를 docker 그룹에 추가
sudo usermod -aG docker $USER

# 재로그인
exit
```

---

## 🔧 배포

### 1. 클론

```bash
git clone <backend-repo-url> manitto-backend
cd manitto-backend
```

### 2. 환경변수 설정

```bash
cp env.example .env
nano .env
```

**.env 설정:**
```env
DB_PASSWORD=your_secure_db_password
JWT_SECRET=생성된_시크릿_키        # openssl rand -hex 32
GOOGLE_CLIENT_ID=your_client_id.apps.googleusercontent.com
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password
DDL_AUTO=validate
SHOW_SQL=false
```

### 3. 실행

```bash
docker compose up -d --build
```

### 4. 확인

```bash
docker compose ps
curl http://localhost:8080/swagger-ui.html
```

---

## ✅ 접속 URL

| 서비스 | URL |
|--------|-----|
| Backend API | `http://YOUR_NCP_IP:8080` |
| Swagger | `http://YOUR_NCP_IP:8080/swagger-ui.html` |

---

## 🔄 업데이트

```bash
git pull
docker compose up -d --build
docker image prune -f
```

---

## 🛠 유용한 명령어

```bash
# 로그 확인
docker compose logs -f backend
docker compose logs -f db

# 재시작
docker compose restart backend

# 중지
docker compose down

# 데이터 포함 삭제 (주의!)
docker compose down -v
```

---

## 🔒 보안 체크리스트

- [ ] NCP ACG에서 5432 포트 차단 (DB 외부 접근 방지)
- [ ] 강력한 DB 비밀번호 사용
- [ ] 새로운 JWT_SECRET 생성
- [ ] 운영환경에서 `DDL_AUTO=validate`
