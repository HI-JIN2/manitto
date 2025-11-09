import { useState } from "react";
import api from "../api/partyApi";
import axios from "axios"; 


export default function JoinParty() {
  const [partyId, setPartyId] = useState("");
  const [email, setEmail] = useState("");

const handleJoin = async (e: React.FormEvent) => {
  e.preventDefault();
  try {
    await api.post(`/parties/${partyId}/join`, { email });
    alert("참여 완료 🎈");
  } catch (error: any) {
    console.error(error);

    if (axios.isAxiosError(error) && error.response?.data?.error) {
      alert(error.response.data.error);
    } else {
      alert("참여 실패 😢");
    }
  }
};

  return (
    <div style={{ padding: 20 }}>
      <h2>🔑 마니또 방 참여</h2>
      <form onSubmit={handleJoin}>
        <input
          placeholder="파티 ID"
          value={partyId}
          onChange={(e) => setPartyId(e.target.value)}
        />
        <input
          placeholder="이메일"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <button type="submit">참여</button>
      </form>
    </div>
  );
}