// AccountPage.jsx
import React from 'react';
import { Card, Button, Typography, Space } from 'antd';
import { LogoutOutlined } from '@ant-design/icons';
import './AccountPage.css';

const { Title, Text } = Typography;

const AccountPage = () => {
    // Пример email пользователя (в реальном приложении это будет из состояния или API)
    const userEmail = 'user@example.com';

    // Функция выхода из аккаунта (заглушка, замените на реальную логику)
    const handleLogout = () => {
        console.log('Выход из аккаунта...');
        // Здесь должна быть логика выхода, например, очистка токенов и перенаправление
    };

    return (
        <div className="account-page-container">
            <Title level={2} style={{ marginBottom: '24px' }}>Аккаунт</Title>

            <Card
                title={<Title level={4} style={{ margin: 0 }}>Информация об аккаунте</Title>}
                style={{ maxWidth: '400px', width: '100%' }}
            >
                <Space direction="vertical" size="large" style={{ width: '100%' }}>
                    <div>
                        <Text type="secondary">Email</Text>
                        <br />
                        <Text strong>{userEmail}</Text>
                    </div>
                    <Button
                        type="primary"
                        icon={<LogoutOutlined />}
                        onClick={handleLogout}
                        block
                        className="logout-button"
                    >
                        Выйти из аккаунта
                    </Button>
                </Space>
            </Card>
        </div>
    );
};

export default AccountPage;