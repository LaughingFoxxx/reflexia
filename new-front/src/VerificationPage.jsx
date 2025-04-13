import React from 'react';
import { Layout, Form, Button, Typography, Input } from 'antd';
import axios from 'axios';
import { useNavigate, useLocation } from 'react-router-dom';
import './VerificationPage.css';

const { Content } = Layout;
const { Title, Text } = Typography;

const VerificationPage = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const email = location.state?.email; // Получаем email из state
    const fromReset = location.state?.fromReset; // Проверяем, пришел ли запрос от сброса пароля

    const onFinish = async (values) => {
        try {
            await axios.post('http://localhost:8082/api/auth/verify', {
                email,
                code: values.otp,
            });
            // Перенаправление в зависимости от контекста
            if (fromReset) {
                // Если это сброс пароля, идем на страницу нового пароля
                navigate('/new-password', { state: { email } });
            } else {
                // Если это регистрация, идем на страницу логина
                navigate('/');
            }
        } catch (error) {
            console.error('Ошибка верификации:', error.response?.data || error.message);
        }
    };

    return (
        <Layout style={{ minHeight: '100vh', background: 'transparent' }}>
            <Content style={{ display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                <div className="verification-container">
                    <div className="verification-header">
                        <Title level={2} style={{ margin: 0, textAlign: 'center' }}>
                            Подтверждение
                        </Title>
                        <Text type="secondary" style={{ display: 'block', textAlign: 'center' }}>
                            Введите 6-значный код, отправленный на ваш email
                        </Text>
                    </div>

                    <Form name="verification" onFinish={onFinish} layout="vertical" style={{ marginTop: 24 }}>
                        <Form.Item
                            name="otp"
                            rules={[
                                { required: true, message: 'Пожалуйста, введите код!' },
                                { len: 6, message: 'Код должен состоять из 6 цифр!' },
                            ]}
                            style={{ textAlign: 'center' }}
                        >
                            <Input.OTP length={6} size="large" className="verification-otp" />
                        </Form.Item>

                        <Form.Item>
                            <Button type="primary" htmlType="submit" size="large" block className="verification-button">
                                Подтвердить
                            </Button>
                        </Form.Item>

                        <div style={{ textAlign: 'center' }}>
                            <Text>
                                Не получили код?{' '}
                                <a href="#" style={{ color: '#1890ff' }}>
                                    Отправить повторно
                                </a>
                            </Text>
                        </div>
                    </Form>
                </div>
            </Content>
        </Layout>
    );
};

export default VerificationPage;