# 릴리즈 가이드

## 자동 릴리즈

`master` 브랜치에 푸시하면 자동으로:
1. 배포가 완료된 후
2. Git 태그가 생성되고 (`vYYYY.MM.DD.BUILD_NUMBER` 형식)
3. GitHub Release가 생성됩니다

## 릴리즈 노트 자동 생성

릴리즈 노트는 다음 순서로 생성됩니다:

1. **CHANGELOG.md의 [Unreleased] 섹션** (우선순위 높음)
2. 마지막 태그 이후의 커밋 메시지

### CHANGELOG.md 작성 방법

```markdown
## [Unreleased]

### Added
- 새로운 기능 추가

### Changed
- 변경된 기능

### Fixed
- 버그 수정
```

## 버전 형식

- 형식: `YYYY.MM.DD.BUILD_NUMBER`
- 예시: `2024.12.20.42`
  - 날짜: 2024년 12월 20일
  - 빌드 번호: 42번째 커밋

## 수동 릴리즈

특정 버전으로 수동 릴리즈를 원하는 경우:

```bash
# 태그 생성
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin v1.0.0

# GitHub에서 Release 생성
```


