import React from 'react';
import { Layout, Form, Input, Button, Typography } from 'antd';
import { MailOutlined } from '@ant-design/icons';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import './ResetPasswordPage.css';

const { Content } = Layout;
const { Title, Text } = Typography;

const ResetPasswordPage = () => {
    const navigate = useNavigate();

    const onFinish = async (values) => {
        try {
            await axios.post('http://localhost:8082/api/auth/forgot-password', {
                email: values.email,
            });
            navigate('/verify', { state: { email: values.email, fromReset: true } });
        } catch (error) {
            console.error('Ошибка сброса пароля:', error.response?.data || error.message);
        }
    };

    return (
        <Layout style={{ minHeight: '100vh', background: 'transparent' }}>
            <Content style={{ display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                <div className="reset-container">
                    <div className="reset-header">
                        <Title level={2} style={{ margin: 0, textAlign: 'center' }}>
                            Сброс пароля
                        </Title>
                        <Text type="secondary" style={{ display: 'block', textAlign: 'center' }}>
                            Введите email, чтобы получить код для сброса пароля
                        </Text>
                    </div>

                    <Form name="reset-password" onFinish={onFinish} layout="vertical" style={{ marginTop: 24 }}>
                        <Form.Item name="email" rules={[{ required: true, message: 'Пожалуйста, введите email!' }, { type: 'email', message: 'Неверный формат email!' }]}>
                            <Input prefix={<MailOutlined />} placeholder="Email" size="large" className="reset-input" />
                        </Form.Item>

                        <Form.Item>
                            <Button type="primary" htmlType="submit" size="large" block className="reset-button">
                                Отправить код
                            </Button>
                        </Form.Item>

                        <div style={{ textAlign: 'center' }}>
                            <Text>
                                Вспомнили пароль?{' '}
                                <a onClick={() => navigate('/')} style={{ color: '#1890ff' }}>
                                    Вернуться к входу
                                </a>
                            </Text>
                        </div>
                    </Form>
                </div>
            </Content>
        </Layout>
    );
};

export default ResetPasswordPage;