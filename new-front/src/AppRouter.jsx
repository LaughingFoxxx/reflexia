import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import AppLanding from './AppLanding'; // Импортируйте ваш лендинг

const AppRouter = () => {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<AppLanding />} />
                <Route path="/text-ai" element={<AppLanding />} />
                {/* Дополнительные маршруты можно добавить здесь */}
            </Routes>
        </Router>
    );
};

export default AppRouter;