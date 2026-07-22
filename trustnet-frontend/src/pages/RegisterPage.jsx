import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

import {
  ArrowRight,
  Eye,
  EyeOff,
  LockKeyhole,
  Mail,
  UserRound,
} from "lucide-react";

import { toast } from "react-toastify";

import AuthLayout from "../components/auth/AuthLayout";
import { useAuth } from "../auth/AuthContext";

function RegisterPage() {
  const navigate = useNavigate();
  const { register } = useAuth();

  const [formData, setFormData] = useState({
    username: "",
    email: "",
    password: "",
    confirmPassword: "",
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

    if (formData.password !== formData.confirmPassword) {
      toast.error("Passwords do not match.");
      return;
    }

    try {
      setIsSubmitting(true);

      await register({
        username: formData.username.trim(),
        email: formData.email.trim(),
        password: formData.password,
      });

      toast.success("Account created successfully. Please sign in.");

      navigate("/login", {
        replace: true,
      });
    } catch (error) {
      const message =
        error.response?.data?.message || "Unable to create your account.";

      toast.error(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <AuthLayout
      eyebrow="Join thoughtfully"
      title="Create your account"
      subtitle="Start building authentic and meaningful connections."
    >
      <form className="auth-form" onSubmit={handleSubmit}>
        <div className="auth-field">
          <label htmlFor="username">Username</label>

          <div className="auth-input-wrapper">
            <UserRound size={19} />

            <input
              id="username"
              name="username"
              type="text"
              placeholder="Choose a username"
              autoComplete="username"
              value={formData.username}
              onChange={handleChange}
              required
            />
          </div>
        </div>

        <div className="auth-field">
          <label htmlFor="register-email">Email address</label>

          <div className="auth-input-wrapper">
            <Mail size={19} />

            <input
              id="register-email"
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
          <label htmlFor="register-password">Password</label>

          <div className="auth-input-wrapper">
            <LockKeyhole size={19} />

            <input
              id="register-password"
              name="password"
              type={showPassword ? "text" : "password"}
              placeholder="Create a secure password"
              autoComplete="new-password"
              minLength={8}
              value={formData.password}
              onChange={handleChange}
              required
            />

            <button
              type="button"
              className="auth-password-toggle"
              onClick={() => setShowPassword((currentValue) => !currentValue)}
            >
              {showPassword ? <EyeOff size={19} /> : <Eye size={19} />}
            </button>
          </div>
        </div>

        <div className="auth-field">
          <label htmlFor="confirm-password">Confirm password</label>

          <div className="auth-input-wrapper">
            <LockKeyhole size={19} />

            <input
              id="confirm-password"
              name="confirmPassword"
              type={showPassword ? "text" : "password"}
              placeholder="Enter the password again"
              autoComplete="new-password"
              minLength={8}
              value={formData.confirmPassword}
              onChange={handleChange}
              required
            />
          </div>
        </div>

        <label className="auth-checkbox">
          <input type="checkbox" required />

          <span>
            I agree to respectful communication and privacy-conscious
            interaction.
          </span>
        </label>

        <button
          type="submit"
          className="auth-primary-button"
          disabled={isSubmitting}
        >
          <span>{isSubmitting ? "Creating account..." : "Create account"}</span>

          {!isSubmitting && <ArrowRight size={18} />}
        </button>
      </form>

      <p className="auth-switch-text">
        Already have an account?
        <Link to="/login">Sign in</Link>
      </p>
    </AuthLayout>
  );
}

export default RegisterPage;
