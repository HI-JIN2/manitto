import { useAuth } from "./context/AuthContext";
import GoogleLoginPopup from "./components/GoogleLoginPopUp";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import CreateParty from "./pages/CreateParty";
import JoinParty from "./pages/JoinParty";
import PartyStatus from "./pages/PartyStatus";
import MatchResult from "./pages/MatchResult";

function App() {
  const { user } = useAuth();

  // ✅ 로그인 안 되어 있으면 Google 로그인 먼저 보여주기
  if (!user) {
    return (
      <div style={{ textAlign: "center", marginTop: "50px" }}>
        <h2>로그인이 필요합니다 🔐</h2>
        <GoogleLoginPopup />
      </div>
    );
  }

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<CreateParty />} />
        <Route path="/party/:partyId/join" element={<JoinParty />} />
        <Route path="/party/:partyId/status" element={<PartyStatus />} />
        <Route path="/party/:partyId/result" element={<MatchResult />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;