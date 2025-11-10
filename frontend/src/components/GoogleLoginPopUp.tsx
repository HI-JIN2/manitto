import { GoogleLogin } from "@react-oauth/google";
import { useAuth } from "../context/AuthContext";
import axios from "axios";

function GoogleLoginPopup() {
  const { login } = useAuth();

  const handleLogin = async (credentialResponse: any) => {
    try {
      const token = credentialResponse.credential;
      const res = await axios.post(
        `${import.meta.env.VITE_API_BASE_URL}/auth/google`,
        { credential: token }
      );

      login(res.data.token); // ✅ 전역 로그인 상태 갱신
      alert("로그인 성공 🎉");
    } catch (err) {
      console.error(err);
      alert("로그인 실패 😢");
    }
  };

  return (
    <GoogleLogin
      onSuccess={handleLogin}
      onError={() => alert("로그인 실패")}
      useOneTap
    />
  );
}

export default GoogleLoginPopup;