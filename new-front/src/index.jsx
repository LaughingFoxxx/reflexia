import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import './index.css';
import App from './App.jsx';
import LoginPage from './LoginPage.jsx';
import LandingPage from './LandingPage.jsx'; // Добавьте импорт
import VerificationPage from './VerificationPage.jsx';
import ResetPasswordPage from './ResetPasswordPage.jsx';
import NewPasswordPage from './NewPasswordPage.jsx';

createRoot(document.getElementById('root')).render(
    <StrictMode>
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<LandingPage />} /> {/* Замените LoginPage на LandingPage */}
                <Route path="/login" element={<LoginPage />} /> {/* Перенесите логин на /login */}
                <Route path="/verify" element={<VerificationPage />} />
                <Route path="/reset-password" element={<ResetPasswordPage />} />
                <Route path="/new-password" element={<NewPasswordPage />} />
                <Route path="/app" element={<App />} />
            </Routes>
        </BrowserRouter>
    </StrictMode>
);