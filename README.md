# Manitto Web

웹 상에서 이메일을 통해 마니또를 배정할 수 있는 서비스

### backend
```
📦manitto
 ┣ 📂config
 ┃ ┣ 📜CorsConfig.kt
 ┃ ┣ 📜OpenApiConfig.kt
 ┃ ┣ 📜SecurityConfig.kt
 ┃ ┗ 📜WebConfig.kt
 ┣ 📂domain
 ┃ ┣ 📂match
 ┃ ┃ ┣ 📜MailService.kt
 ┃ ┃ ┣ 📜MatchController.kt
 ┃ ┃ ┣ 📜MatchService.kt
 ┃ ┃ ┗ 📜MatchedResultRepository.kt
 ┃ ┣ 📂participant
 ┃ ┃ ┣ 📜ParticipantController.kt
 ┃ ┃ ┣ 📜ParticipantRepository.kt
 ┃ ┃ ┣ 📜ParticipantResponse.kt
 ┃ ┃ ┗ 📜ParticipantService.kt
 ┃ ┣ 📂party
 ┃ ┃ ┣ 📜PartyController.kt
 ┃ ┃ ┣ 📜PartyRepository.kt
 ┃ ┃ ┗ 📜PartyService.kt
 ┃ ┗ 📂user
 ┃ ┃ ┣ 📜AuthController.kt
 ┃ ┃ ┣ 📜JwtService.kt
 ┃ ┃ ┗ 📜UserRepository.kt
 ┣ 📂global
 ┃ ┣ 📂entity
 ┃ ┃ ┣ 📜MatchedResult.kt
 ┃ ┃ ┣ 📜Participant.kt
 ┃ ┃ ┣ 📜Party.kt
 ┃ ┃ ┗ 📜User.kt
 ┃ ┣ 📜GlobalExceptionHandler.kt
 ┃ ┗ 📜JwtAuthFilter.kt
 ┗ 📜ManittoApplication.kt
```

 ### frontend
```
📦src
 ┣ 📂api
 ┃ ┗ 📜partyApi.ts
 ┣ 📂assets
 ┃ ┗ 📜react.svg
 ┣ 📂components
 ┃ ┗ 📜GoogleLoginPopUp.tsx
 ┣ 📂context
 ┃ ┗ 📜AuthContext.tsx
 ┣ 📂pages
 ┃ ┣ 📜CreateParty.tsx
 ┃ ┣ 📜JoinParty.tsx
 ┃ ┣ 📜MatchResult.tsx
 ┃ ┗ 📜PartyStatus.tsx
 ┣ 📂styles
 ┃ ┗ 📜global.css
 ┣ 📜App.css
 ┣ 📜App.tsx
 ┣ 📜index.css
 ┗ 📜main.tsx
```
