import { useState } from "react";

export default function CreateParty() {
  const [partyName, setPartyName] = useState("");
  const [password, setPassword] = useState("");

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    alert(`방 생성: ${partyName} / 비번: ${password}`);
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