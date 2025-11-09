import { useState } from "react";
import api from "../api/partyApi";

export default function MatchResult() {
  const [result, setResult] = useState<string | null>(null);

  const fetchResult = async () => {
    try {
      const res = await api.get("/parties/match/result");
      setResult(res.data.receiver);
    } catch (error) {
      console.error(error);
    }
  };

  return (
    <div style={{ padding: 20 }}>
      <h2>🎁 당신의 마니또는...</h2>
      <button onClick={fetchResult}>결과 보기</button>
      {result && <p>{result}</p>}
    </div>
  );
}