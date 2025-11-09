import { useState } from "react";
import api from "../api/partyApi";

export default function PartyStatus() {
  const [status, setStatus] = useState<any>(null);

  const fetchStatus = async () => {
    try {
      const res = await api.get("/parties/status");
      setStatus(res.data);
    } catch (error) {
      console.error(error);
    }
  };

  return (
    <div style={{ padding: 20 }}>
      <h2>👥 파티 대기중...</h2>
      <button onClick={fetchStatus}>상태 확인</button>
      {status && <pre>{JSON.stringify(status, null, 2)}</pre>}
    </div>
  );
}