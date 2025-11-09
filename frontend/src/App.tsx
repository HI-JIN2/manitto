import { BrowserRouter, Routes, Route } from "react-router-dom";
import CreateParty from "./pages/CreateParty.tsx";
import JoinParty from "./pages/JoinParty.tsx";
import PartyStatus from "./pages/PartyStatus.tsx";
import MatchResult from "./pages/MatchResult.tsx";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<CreateParty />} />
        <Route path="/join" element={<JoinParty />} />
        <Route path="/status" element={<PartyStatus />} />
        <Route path="/result" element={<MatchResult />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;