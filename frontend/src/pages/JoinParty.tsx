import { useAuth } from "../context/AuthContext";
import { useState } from "react";
import api from "../api/partyApi";
import axios from "axios";
import { useParams } from "react-router-dom";

export default function JoinParty() {

  const { user, logout } = useAuth();
  const { partyId } = useParams();

  const handleJoin = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user) {
      alert("로그인이 필요합니다 😢");
      return;
    }
    try {

      console.log("보낼 이메일:", user);

      await api.post(`/parties/${partyId}/join`, { email: user.sub });
      // await api.post(`/parties/${partyId}/join`, { email: user.email });
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
        {/* <input
          placeholder="파티 ID"
          value={partyId}
          onChange={(e) => setPartyId(e.target.value)}
        /> */}

        <button type="submit">참여</button>
      </form>
    </div>
  );
}