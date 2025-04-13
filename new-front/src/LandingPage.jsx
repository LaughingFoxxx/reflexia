import React from 'react';
import { Layout, Button, Typography, Space } from 'antd';
import { FileTextOutlined, SettingOutlined, UserOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import './LandingPage.css';

const { Content } = Layout;
const { Title, Text } = Typography;

const LandingPage = () => {
    const navigate = useNavigate();

    return (
        <Layout className="landing-layout">
            <Content className="landing-content">
                {/* Hero Section */}
                <div className="hero-section">
                    <div className="glow-effect" />
                    <Title level={1} className="hero-title">
                        Рефлексия
                    </Title>
                    <Text className="hero-subtitle">
                        Ваш умный помощник для работы с текстом. Создавайте, редактируйте и улучшайте свои документы с помощью искусственного интеллекта.
                    </Text>
                    <Space size="large" className="hero-buttons">
                        <Button
                            type="primary"
                            size="large"
                            onClick={() => navigate('/app')}
                            className="cta-button"
                        >
                            Попробовать сейчас
                        </Button>
                    </Space>
                </div>

                {/* Features Section */}
                <div className="features-section">
                    <div className="features-container">
                        <div className="feature-card">
                            <FileTextOutlined className="feature-icon" />
                            <Title level={4} className="feature-title">
                                Умная обработка текста
                            </Title>
                            <Text className="feature-text">
                                Улучшайте свои тексты с помощью ИИ: повышайте ясность, сокращайте объем, переводите на другие языки или генерируйте новые идеи — все это одним кликом.
                            </Text>
                        </div>
                        <div className="feature-card">
                            <SettingOutlined className="feature-icon" />
                            <Title level={4} className="feature-title">
                                Персонализация
                            </Title>
                            <Text className="feature-text">
                                Настройте интерфейс и редактор под свои нужды: выбирайте темную тему, регулируйте размер шрифта и стиль, чтобы работать максимально комфортно.
                            </Text>
                        </div>
                        <div className="feature-card">
                            <UserOutlined className="feature-icon" />
                            <Title level={4} className="feature-title">
                                Простота использования
                            </Title>
                            <Text className="feature-text">
                                Интуитивный дизайн и мощные функции в одном. Создавайте документы, анализируйте текст и получайте результаты без лишних сложностей.
                            </Text>
                        </div>
                    </div>
                </div>
            </Content>
        </Layout>
    );
};

export default LandingPage;