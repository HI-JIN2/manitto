import { useState, useEffect } from "react";
import axios from "axios";

function PartyStatus() {
  const [participants, setParticipants] = useState<{ id: number; email: string }[]>([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState("");
  const [isMatched, setIsMatched] = useState(false); // ✅ 매칭 상태 추가
  const partyId = 1; // 나중에 URL param으로 변경 가능

  // 참여자 목록 + 매칭 상태 동시에 불러오기
  useEffect(() => {
    const fetchPartyData = async () => {
      try {
        // 참여자 목록
        const participantsRes = await axios.get(
          `${import.meta.env.VITE_API_BASE_URL}/parties/${partyId}/participants`
        );
        setParticipants(participantsRes.data);

        // 매칭 상태
        const statusRes = await axios.get(
          `${import.meta.env.VITE_API_BASE_URL}/parties/${partyId}/status`
        );
        setIsMatched(statusRes.data.matched);
      } catch (err) {
        console.error("파티 정보 불러오기 실패:", err);
        setMessage("파티 정보를 불러올 수 없습니다 😢");
      } finally {
        setLoading(false);
      }
    };

    fetchPartyData();
  }, [partyId]);

  // 매칭 시작 요청
  const handleMatch = async () => {
    try {
      const res = await axios.post(
        `${import.meta.env.VITE_API_BASE_URL}/parties/${partyId}/match`
      );
      setMessage("매칭 완료! 이메일이 발송되었습니다 ✉️");
      setIsMatched(true); // ✅ UI 상태 업데이트
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

      {/* ✅ 매칭 상태에 따른 UI 분기 */}
      {isMatched ? (
        <div style={{ marginTop: 20, color: "green", fontWeight: "bold" }}>
          🎁 이미 매칭이 완료된 파티입니다!
        </div>
      ) : (
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
      )}

      {message && <p style={{ marginTop: 20 }}>{message}</p>}
    </div>
  );
}

export default PartyStatus;