import React, { useState } from 'react';
import { Layout, Form, Input, Button, Typography } from 'antd';
import { UserOutlined, LockOutlined, MailOutlined } from '@ant-design/icons';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import './LoginPage.css';

const { Content } = Layout;
const { Title, Text, Link } = Typography;

const LoginPage = () => {
    const [isLogin, setIsLogin] = useState(true);
    const navigate = useNavigate();

    const onFinish = async (values) => {
        try {
            if (isLogin) {
                // Логин
                await axios.post('http://localhost:8082/api/auth/login', {
                    email: values.email,
                    password: values.password,
                }, {
                    withCredentials: true // Важно для работы с HttpOnly куками
                });
                navigate('/app');
            } else {
                // Регистрация
                await axios.post('http://localhost:8082/api/auth/register', {
                    email: values.email,
                    password: values.password,
                }, {
                    withCredentials: true // Важно для работы с HttpOnly куками
                });
                navigate('/verify', { state: { email: values.email } });
            }
        } catch (error) {
            console.error('Ошибка:', error.response?.data || error.message);
        }
    };

    return (
        <Layout style={{ minHeight: '100vh', background: 'transparent' }}>
            <Content style={{ display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                <div className="auth-container">
                    <div className="auth-header">
                        <Title level={2} style={{ margin: 0, textAlign: 'center' }}>
                            {isLogin ? 'Вход' : 'Регистрация'}
                        </Title>
                        <Text type="secondary" style={{ display: 'block', textAlign: 'center' }}>
                            {isLogin ? 'Добро пожаловать обратно!' : 'Создайте новый аккаунт'}
                        </Text>
                    </div>

                    <Form name={isLogin ? 'login' : 'register'} initialValues={{ remember: true }} onFinish={onFinish} layout="vertical" style={{ marginTop: 24 }}>
                        <Form.Item name="email" rules={[{ required: true, message: 'Пожалуйста, введите email!' }, { type: 'email', message: 'Неверный формат email!' }]}>
                            <Input prefix={<MailOutlined />} placeholder="Email" size="large" className="auth-input" />
                        </Form.Item>

                        <Form.Item name="password" rules={[{ required: true, message: 'Пожалуйста, введите пароль!' }]}>
                            <Input.Password prefix={<LockOutlined />} placeholder="Пароль" size="large" className="auth-input" />
                        </Form.Item>

                        {isLogin && (
                            <Form.Item>
                                <Link onClick={() => navigate('/reset-password')} style={{ color: '#1890ff' }}>
                                    Не помню пароль
                                </Link>
                            </Form.Item>
                        )}

                        <Form.Item>
                            <Button type="primary" htmlType="submit" size="large" block className="auth-button">
                                {isLogin ? 'Войти' : 'Зарегистрироваться'}
                            </Button>
                        </Form.Item>

                        <div style={{ textAlign: 'center' }}>
                            <Text>
                                {isLogin ? 'Нет аккаунта?' : 'Уже есть аккаунт?'}{' '}
                                <Link onClick={() => setIsLogin(!isLogin)} style={{ color: '#1890ff' }}>
                                    {isLogin ? 'Зарегистрироваться' : 'Войти'}
                                </Link>
                            </Text>
                        </div>
                    </Form>
                </div>
            </Content>
        </Layout>
    );
};

export default LoginPage;