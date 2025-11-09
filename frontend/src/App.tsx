import { BrowserRouter, Routes, Route } from "react-router-dom";
import CreateParty from "./pages/CreateParty";
import JoinParty from "./pages/JoinParty";
import PartyStatus from "./pages/PartyStatus";
import MatchResult from "./pages/MatchResult";

function App() {
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