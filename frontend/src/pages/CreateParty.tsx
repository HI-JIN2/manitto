import { useState } from "react";
import api from "../api/partyApi";

export default function CreateParty() {
  const [partyName, setPartyName] = useState("");
  const [password, setPassword] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const res = await api.post("/parties", {
        name: partyName,
        password,
      });
      alert(`파티 생성 완료! ID: ${res.data.id}`);
    } catch (error) {
      console.error(error);
      alert("파티 생성 실패 😢");
    }
  };

  return (
    <div style={{ padding: 20 }}>
      <h2>🎉 마니또 방 만들기</h2>
      <form onSubmit={handleSubmit}>
        <input
          placeholder="방 이름"
          value={partyName}
          onChange={(e) => setPartyName(e.target.value)}
        />
        <input
          type="password"
          placeholder="비밀번호"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
        <button type="submit">생성</button>
      </form>
    </div>
  );
}