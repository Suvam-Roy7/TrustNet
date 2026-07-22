import { motion } from "motion/react";
import {
  HeartHandshake,
  LockKeyhole,
  ShieldCheck,
  UsersRound,
} from "lucide-react";

import "../../styles/auth.css";

function AuthLayout({ children, eyebrow, title, subtitle }) {
  return (
    <main className="auth-shell">
      <section className="auth-story-panel">
        <div className="auth-story-decoration auth-story-decoration-one" />
        <div className="auth-story-decoration auth-story-decoration-two" />

        <div className="auth-story-content">
          <div className="auth-brand">
            <div className="auth-brand-icon">
              <ShieldCheck size={24} strokeWidth={1.8} />
            </div>

            <span>TrustNet</span>
          </div>

          <motion.div
            className="auth-story-copy"
            initial={{ opacity: 0, y: 24 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.65 }}
          >
            <p className="auth-story-eyebrow">Social, without the noise</p>

            <h1>Meaningful connections deserve a calmer space.</h1>

            <p className="auth-story-description">
              TrustNet is built around privacy, authenticity and purposeful
              interaction—not endless attention seeking.
            </p>

            <div className="auth-benefit-list">
              <div className="auth-benefit">
                <LockKeyhole size={19} />

                <div>
                  <strong>Privacy first</strong>
                  <span>Your identity and interactions stay protected.</span>
                </div>
              </div>

              <div className="auth-benefit">
                <UsersRound size={19} />

                <div>
                  <strong>Authentic connections</strong>
                  <span>Build relationships that genuinely matter.</span>
                </div>
              </div>

              <div className="auth-benefit">
                <HeartHandshake size={19} />

                <div>
                  <strong>Purposeful experience</strong>
                  <span>Less distraction. More meaningful communication.</span>
                </div>
              </div>
            </div>
          </motion.div>

          <p className="auth-story-footer">Private. Purposeful. Human.</p>
        </div>
      </section>

      <section className="auth-form-panel">
        <motion.div
          className="auth-form-container"
          initial={{ opacity: 0, x: 26 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.55, delay: 0.08 }}
        >
          <div className="auth-mobile-brand">
            <div className="auth-mobile-brand-icon">
              <ShieldCheck size={22} strokeWidth={1.8} />
            </div>

            <span>TrustNet</span>
          </div>

          <header className="auth-form-header">
            <p>{eyebrow}</p>
            <h2>{title}</h2>
            <span>{subtitle}</span>
          </header>

          {children}
        </motion.div>
      </section>
    </main>
  );
}

export default AuthLayout;
