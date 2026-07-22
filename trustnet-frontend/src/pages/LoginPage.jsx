import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

import { ArrowRight, Eye, EyeOff, LockKeyhole, Mail } from "lucide-react";

import { toast } from "react-toastify";

import AuthLayout from "../components/auth/AuthLayout";
import { useAuth } from "../auth/AuthContext";

function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [formData, setFormData] = useState({
    email: "",
    password: "",
  });

  const [showPassword, setShowPassword] = useState(false);

  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleChange = (event) => {
    const { name, value } = event.target;

    setFormData((currentData) => ({
      ...currentData,
      [name]: value,
    }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    try {
      setIsSubmitting(true);

      await login({
        email: formData.email.trim(),
        password: formData.password,
      });

      toast.success("Welcome back to TrustNet.");

      navigate("/home", {
        replace: true,
      });
    } catch (error) {
      console.error("Login failed:", error.response?.data || error);

      const message =
        error.response?.data?.message || "Invalid email or password.";

      toast.error(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <AuthLayout
      eyebrow="Welcome back"
      title="Sign in to TrustNet"
      subtitle="Continue to your private and purposeful social space."
    >
      <form className="auth-form" onSubmit={handleSubmit}>
        <div className="auth-field">
          <label htmlFor="email">Email address</label>

          <div className="auth-input-wrapper">
            <Mail size={19} />

            <input
              id="email"
              name="email"
              type="email"
              placeholder="you@example.com"
              autoComplete="email"
              value={formData.email}
              onChange={handleChange}
              required
            />
          </div>
        </div>

        <div className="auth-field">
          <div className="auth-label-row">
            <label htmlFor="password">Password</label>

            <button type="button" className="auth-text-button">
              Forgot password?
            </button>
          </div>

          <div className="auth-input-wrapper">
            <LockKeyhole size={19} />

            <input
              id="password"
              name="password"
              type={showPassword ? "text" : "password"}
              placeholder="Enter your password"
              autoComplete="current-password"
              value={formData.password}
              onChange={handleChange}
              required
            />

            <button
              type="button"
              className="auth-password-toggle"
              aria-label={showPassword ? "Hide password" : "Show password"}
              onClick={() => setShowPassword((currentValue) => !currentValue)}
            >
              {showPassword ? <EyeOff size={19} /> : <Eye size={19} />}
            </button>
          </div>
        </div>

        <label className="auth-checkbox">
          <input type="checkbox" />
          <span>Keep me signed in on this device</span>
        </label>

        <button
          type="submit"
          className="auth-primary-button"
          disabled={isSubmitting}
        >
          <span>{isSubmitting ? "Signing in..." : "Sign in"}</span>

          {!isSubmitting && <ArrowRight size={18} />}
        </button>
      </form>

      <p className="auth-switch-text">
        New to TrustNet?
        <Link to="/register">Create an account</Link>
      </p>

      <p className="auth-security-note">
        Your account is protected through secure authentication.
      </p>
    </AuthLayout>
  );
}

export default LoginPage;
