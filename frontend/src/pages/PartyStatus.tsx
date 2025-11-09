import { useState, useEffect } from "react";
import axios from "axios";

function PartyStatus() {
  const [participants, setParticipants] = useState<{ id: number; email: string }[]>([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState("");
  const partyId = 1; // 나중에 URL param으로 변경할 수 있음

  // 참여자 목록 불러오기
  useEffect(() => {
    const fetchParticipants = async () => {
      try {
        const res = await axios.get(
          `${import.meta.env.VITE_API_BASE_URL}/parties/${partyId}/participants`
        );
        setParticipants(res.data);
      } catch (err) {
        console.error("참여자 불러오기 실패:", err);
      } finally {
        setLoading(false);
      }
    };

    fetchParticipants();
  }, []);

  // 매칭 시작 요청
  const handleMatch = async () => {
    try {
      const res = await axios.post(
        `${import.meta.env.VITE_API_BASE_URL}/parties/${partyId}/match`
      );
      setMessage("매칭 완료! 이메일이 발송되었습니다 ✉️");
      console.log(res.data);
    } catch (err) {
      console.error("매칭 중 오류:", err);
      setMessage("매칭 실패 😢");
    }
  };

  if (loading) return <div style={{ padding: 20 }}>불러오는 중...</div>;

  return (
    <div style={{ padding: 20 }}>
      <h2>👥 파티 참가자 목록</h2>
      {participants.length === 0 ? (
        <p>아직 참가자가 없습니다.</p>
      ) : (
        <ul>
          {participants.map((p) => (
            <li key={p.id}>{p.email}</li>
          ))}
        </ul>
      )}

      <button
        onClick={handleMatch}
        style={{
          marginTop: 20,
          padding: "10px 20px",
          borderRadius: 10,
          backgroundColor: "#0078FF",
          color: "white",
          border: "none",
          cursor: "pointer",
        }}
      >
        매칭 시작 🎁
      </button>

      {message && <p style={{ marginTop: 20 }}>{message}</p>}
    </div>
  );
}

export default PartyStatus;