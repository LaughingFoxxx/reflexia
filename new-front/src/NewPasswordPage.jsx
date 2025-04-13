import React from 'react';
import { Layout, Form, Input, Button, Typography } from 'antd';
import {LockOutlined} from '@ant-design/icons';
import axios from 'axios';
import { useNavigate, useLocation } from 'react-router-dom';
import './NewPasswordPage.css';

const { Content } = Layout;
const { Title, Text } = Typography;

const NewPasswordPage = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const email = location.state?.email;

    const onFinish = async (values) => {
        try {
            await axios.post('http://localhost:8082/api/auth/get-new-password', {
                email,
                password: values.password,
            });
            navigate('/');
        } catch (error) {
            console.error('Ошибка установки нового пароля:', error.response?.data || error.message);
        }
    };

    return (
        <Layout style={{ minHeight: '100vh', background: 'transparent' }}>
            <Content style={{ display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                <div className="new-password-container">
                    <div className="new-password-header">
                        <Title level={2} style={{ margin: 0, textAlign: 'center' }}>
                            Новый пароль
                        </Title>
                        <Text type="secondary" style={{ display: 'block', textAlign: 'center' }}>
                            Введите новый пароль для вашей учетной записи
                        </Text>
                    </div>

                    <Form name="new-password" onFinish={onFinish} layout="vertical" style={{ marginTop: 24 }}>
                        <Form.Item name="password" rules={[{ required: true, message: 'Пожалуйста, введите новый пароль!' }, { min: 8, message: 'Пароль должен содержать минимум 8 символов!' }]}>
                            <Input.Password prefix={<LockOutlined />} placeholder="Новый пароль" size="large" className="new-password-input" />
                        </Form.Item>

                        <Form.Item
                            name="confirmPassword"
                            dependencies={['password']}
                            rules={[
                                { required: true, message: 'Пожалуйста, подтвердите пароль!' },
                                ({ getFieldValue }) => ({
                                    validator(_, value) {
                                        if (!value || getFieldValue('password') === value) {
                                            return Promise.resolve();
                                        }
                                        return Promise.reject(new Error('Пароли не совпадают!'));
                                    },
                                }),
                            ]}
                        >
                            <Input.Password prefix={<LockOutlined />} placeholder="Подтвердите пароль" size="large" className="new-password-input" />
                        </Form.Item>

                        <Form.Item>
                            <Button type="primary" htmlType="submit" size="large" block className="new-password-button">
                                Сохранить
                            </Button>
                        </Form.Item>

                        <div style={{ textAlign: 'center' }}>
                            <Text>
                                Передумали?{' '}
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

export default NewPasswordPage;