import React from 'react';
import { Layout, Typography, Row, Col, Card, Button, List, Avatar, Form, Input, Divider, Space } from 'antd';
import {
    ThunderboltFilled,
    CheckCircleFilled,
    BulbFilled,
    RobotFilled,
    MailOutlined,
    UserOutlined,
    ArrowRightOutlined,
    CrownFilled,
    StarFilled,
    SafetyCertificateFilled
} from '@ant-design/icons';
import { useInView } from 'react-intersection-observer';
import { animated, useSpring } from '@react-spring/web';

const { Header, Content, Footer } = Layout;
const { Title, Paragraph, Text } = Typography;

// Анимированный компонент
const AnimatedCard = ({ children, delay = 0 }) => {
    const [ref, inView] = useInView({
        triggerOnce: true,
        threshold: 0.1,
    });

    const animation = useSpring({
        opacity: inView ? 1 : 0,
        transform: inView ? 'translateY(0)' : 'translateY(20px)',
        delay: delay,
    });

    return (
        <animated.div ref={ref} style={animation}>
            {children}
        </animated.div>
    );
};

const features = [
    {
        title: 'Автоматическое исправление',
        description: 'ИИ исправляет орфографические и грамматические ошибки',
        icon: <CheckCircleFilled style={{ fontSize: '32px', color: '#52c41a' }} />
    },
    {
        title: 'Перефразирование',
        description: 'Переписывает текст, сохраняя смысл, но меняя формулировки',
        icon: <BulbFilled style={{ fontSize: '32px', color: '#faad14' }} />
    },
    {
        title: 'Краткое содержание',
        description: 'Создает краткие выжимки из больших текстов',
        icon: <ThunderboltFilled style={{ fontSize: '32px', color: '#1890ff' }} />
    },
    {
        title: 'Генерация текста',
        description: 'Создает новый текст на основе ваших идей и ключевых слов',
        icon: <RobotFilled style={{ fontSize: '32px', color: '#722ed1' }} />
    }
];

const testimonials = [
    {
        name: 'Анна К.',
        role: 'Копирайтер',
        text: 'Это приложение сократило время редактирования моих текстов на 70%!',
        rating: 5
    },
    {
        name: 'Максим П.',
        role: 'Студент',
        text: 'Лучший инструмент для подготовки рефератов и курсовых работ.',
        rating: 4
    },
    {
        name: 'Елена С.',
        role: 'Блогер',
        text: 'Теперь я трачу меньше времени на правки и больше на создание контента.',
        rating: 5
    }
];

const pricingPlans = [
    {
        name: 'Базовый',
        price: '₽990',
        period: 'в месяц',
        features: ['До 10 000 символов', '5 документов в день', 'Базовые исправления'],
        popular: false
    },
    {
        name: 'Профессиональный',
        price: '₽2990',
        period: 'в месяц',
        features: ['До 50 000 символов', '20 документов в день', 'Все функции', 'Приоритетная поддержка'],
        popular: true
    },
    {
        name: 'Корпоративный',
        price: '₽8990',
        period: 'в месяц',
        features: ['Неограниченный объем', 'Неограниченные документы', 'Все функции', 'Персональный менеджер'],
        popular: false
    }
];

const AppLanding = () => {
    const [form] = Form.useForm();

    const onFinish = (values) => {
        console.log('Received values:', values);
        // Здесь можно добавить логику отправки формы
    };

    return (
        <Layout className="layout" style={{ minHeight: '100vh', background: '#f8f9fa' }}>
            <Header style={{
                background: 'linear-gradient(135deg, #1890ff 0%, #096dd9 100%)',
                padding: '0 24px',
                boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
                position: 'sticky',
                top: 0,
                zIndex: 1
            }}>
                <Row justify="space-between" align="middle" style={{ maxWidth: 1200, margin: '0 auto', width: '100%' }}>
                    <Col>
                        <Space align="center">
                            <Title level={3} style={{ margin: 0, color: '#fff' }}>
                                <RobotFilled style={{ marginRight: 8 }} />
                                TextAI Pro
                            </Title>
                        </Space>
                    </Col>
                    <Col>
                        <Button type="primary" size="large" ghost>
                            Попробовать бесплатно
                        </Button>
                    </Col>
                </Row>
            </Header>

            <Content style={{ padding: '0 24px', maxWidth: 1200, margin: '0 auto', width: '100%' }}>
                {/* Hero Section */}
                <Row
                    gutter={[48, 48]}
                    align="middle"
                    style={{
                        margin: '80px 0',
                        textAlign: 'center',
                        justifyContent: 'center'
                    }}
                >
                    <Col xs={24} md={16}>
                        <AnimatedCard>
                            <div style={{ marginBottom: 24 }}>
                                <Text strong style={{
                                    color: '#1890ff',
                                    fontSize: 18,
                                    letterSpacing: 2,
                                    textTransform: 'uppercase'
                                }}>
                                    ИННОВАЦИОННОЕ РЕШЕНИЕ
                                </Text>
                            </div>
                            <Title level={1} style={{ fontSize: '48px', marginBottom: 24 }}>
                                Преобразуйте ваш текст с помощью <span style={{ color: '#1890ff' }}>искусственного интеллекта</span>
                            </Title>
                            <Paragraph style={{ fontSize: 20, marginBottom: 48 }}>
                                Наше приложение использует передовые алгоритмы искусственного интеллекта для анализа,
                                улучшения и преобразования ваших текстов с беспрецедентной точностью.
                            </Paragraph>
                            <Space size="large">
                                <Button type="primary" size="large" shape="round" icon={<ThunderboltFilled />}>
                                    Начать бесплатно
                                </Button>
                                <Button size="large" shape="round">
                                    Узнать больше <ArrowRightOutlined />
                                </Button>
                            </Space>
                        </AnimatedCard>
                    </Col>
                </Row>

                {/* Logo Cloud */}
                <Divider>
                    <Text type="secondary">Используется ведущими компаниями</Text>
                </Divider>
                <Row gutter={[24, 24]} justify="center" style={{ marginBottom: 80 }}>
                    {['Company A', 'Company B', 'Company C', 'Company D'].map((company, index) => (
                        <Col key={index}>
                            <div style={{
                                padding: '16px 32px',
                                background: '#fff',
                                borderRadius: 8,
                                boxShadow: '0 2px 8px rgba(0,0,0,0.05)'
                            }}>
                                <Text strong>{company}</Text>
                            </div>
                        </Col>
                    ))}
                </Row>

                {/* Features Section */}
                <div style={{ marginBottom: 80 }}>
                    <Divider orientation="center">
                        <Title level={2} style={{ fontSize: 32 }}>Мощные возможности</Title>
                        <Paragraph type="secondary">Все, что нужно для идеального текста</Paragraph>
                    </Divider>
                    <Row gutter={[24, 24]} justify="center">
                        {features.map((feature, index) => (
                            <Col xs={24} sm={12} md={6} key={index}>
                                <AnimatedCard delay={index * 100}>
                                    <Card
                                        hoverable
                                        style={{
                                            height: '100%',
                                            borderRadius: 12,
                                            border: 'none',
                                            boxShadow: '0 4px 12px rgba(0,0,0,0.05)'
                                        }}
                                        bodyStyle={{ padding: 24 }}
                                    >
                                        <div style={{ marginBottom: 16 }}>
                                            {feature.icon}
                                        </div>
                                        <Title level={4} style={{ marginBottom: 8 }}>{feature.title}</Title>
                                        <Paragraph type="secondary">{feature.description}</Paragraph>
                                    </Card>
                                </AnimatedCard>
                            </Col>
                        ))}
                    </Row>
                </div>

                {/* Demo Section */}
                <Row gutter={[48, 48]} align="middle" style={{ marginBottom: 80 }}>
                    <Col xs={24} md={12}>
                        <AnimatedCard>
                            <img
                                src="https://via.placeholder.com/600x400?text=AI+Text+Demo"
                                alt="AI Text Demo"
                                style={{
                                    width: '100%',
                                    borderRadius: 16,
                                    boxShadow: '0 8px 24px rgba(0,0,0,0.1)',
                                    border: '1px solid #f0f0f0'
                                }}
                            />
                        </AnimatedCard>
                    </Col>
                    <Col xs={24} md={12}>
                        <AnimatedCard delay={200}>
                            <Title level={2} style={{ marginBottom: 24 }}>Посмотрите, как это работает</Title>
                            <Paragraph style={{ fontSize: 16, marginBottom: 24 }}>
                                Наш ИИ анализирует текст на нескольких уровнях, обеспечивая комплексное улучшение:
                            </Paragraph>
                            <List
                                itemLayout="horizontal"
                                dataSource={[
                                    'Грамматика и орфография',
                                    'Стиль и читаемость',
                                    'Тон и эмоциональная окраска',
                                    'Структура и логика изложения'
                                ]}
                                renderItem={(item) => (
                                    <List.Item>
                                        <List.Item.Meta
                                            avatar={<CheckCircleFilled style={{ color: '#52c41a', fontSize: 20 }} />}
                                            title={<Text>{item}</Text>}
                                        />
                                    </List.Item>
                                )}
                            />
                            <Button type="primary" size="large" style={{ marginTop: 24 }} shape="round">
                                Посмотреть демо <ArrowRightOutlined />
                            </Button>
                        </AnimatedCard>
                    </Col>
                </Row>

                {/* Testimonials */}
                <div style={{ marginBottom: 80 }}>
                    <Divider orientation="center">
                        <Title level={2} style={{ fontSize: 32 }}>Отзывы наших клиентов</Title>
                        <Paragraph type="secondary">Более 10,000 довольных пользователей</Paragraph>
                    </Divider>
                    <Row gutter={[24, 24]} justify="center">
                        {testimonials.map((testimonial, index) => (
                            <Col xs={24} md={8} key={index}>
                                <AnimatedCard delay={index * 100}>
                                    <Card
                                        style={{
                                            height: '100%',
                                            borderRadius: 12,
                                            border: 'none',
                                            boxShadow: '0 4px 12px rgba(0,0,0,0.05)'
                                        }}
                                        bodyStyle={{ padding: 24 }}
                                    >
                                        <div style={{ display: 'flex', marginBottom: 16 }}>
                                            {[...Array(testimonial.rating)].map((_, i) => (
                                                <StarFilled key={i} style={{ color: '#faad14', fontSize: 16, marginRight: 4 }} />
                                            ))}
                                        </div>
                                        <Paragraph style={{ fontSize: 16, fontStyle: 'italic', marginBottom: 24 }}>
                                            "{testimonial.text}"
                                        </Paragraph>
                                        <Space>
                                            <Avatar size="large" icon={<UserOutlined />} />
                                            <div>
                                                <Text strong>{testimonial.name}</Text>
                                                <br />
                                                <Text type="secondary">{testimonial.role}</Text>
                                            </div>
                                        </Space>
                                    </Card>
                                </AnimatedCard>
                            </Col>
                        ))}
                    </Row>
                </div>

                {/* Pricing */}
                <div style={{ marginBottom: 80 }}>
                    <Divider orientation="center">
                        <Title level={2} style={{ fontSize: 32 }}>Прозрачные тарифы</Title>
                        <Paragraph type="secondary">Выберите подходящий план</Paragraph>
                    </Divider>
                    <Row gutter={[24, 24]} justify="center">
                        {pricingPlans.map((plan, index) => (
                            <Col xs={24} md={8} key={index}>
                                <AnimatedCard delay={index * 100}>
                                    <Card
                                        hoverable
                                        style={{
                                            height: '100%',
                                            borderRadius: 12,
                                            border: plan.popular ? '2px solid #1890ff' : '1px solid #f0f0f0',
                                            position: 'relative',
                                            overflow: 'hidden'
                                        }}
                                        bodyStyle={{ padding: 24 }}
                                    >
                                        {plan.popular && (
                                            <div style={{
                                                position: 'absolute',
                                                top: 0,
                                                right: 0,
                                                background: '#1890ff',
                                                color: '#fff',
                                                padding: '4px 16px',
                                                borderRadius: '0 0 0 12px',
                                                fontSize: 12,
                                                fontWeight: 'bold'
                                            }}>
                                                <CrownFilled style={{ marginRight: 4 }} /> ПОПУЛЯРНЫЙ
                                            </div>
                                        )}
                                        <Title level={3} style={{ marginBottom: 8 }}>{plan.name}</Title>
                                        <div style={{ marginBottom: 24 }}>
                                            <Text strong style={{ fontSize: 32 }}>{plan.price}</Text>
                                            <Text type="secondary"> / {plan.period}</Text>
                                        </div>
                                        <List
                                            dataSource={plan.features}
                                            renderItem={(item) => (
                                                <List.Item>
                                                    <List.Item.Meta
                                                        avatar={<CheckCircleFilled style={{ color: '#52c41a' }} />}
                                                        title={<Text>{item}</Text>}
                                                    />
                                                </List.Item>
                                            )}
                                        />
                                        <Button
                                            type={plan.popular ? 'primary' : 'default'}
                                            block
                                            size="large"
                                            style={{ marginTop: 24 }}
                                        >
                                            Выбрать план
                                        </Button>
                                    </Card>
                                </AnimatedCard>
                            </Col>
                        ))}
                    </Row>
                </div>

                {/* CTA Section */}
                <AnimatedCard delay={100}>
                    <div style={{
                        background: 'linear-gradient(135deg, #1890ff 0%, #096dd9 100%)',
                        borderRadius: 16,
                        padding: '48px 24px',
                        textAlign: 'center',
                        marginBottom: 80
                    }}>
                        <Title level={2} style={{ color: '#fff', marginBottom: 16 }}>
                            Готовы улучшить свои тексты?
                        </Title>
                        <Paragraph style={{ color: 'rgba(255,255,255,0.85)', fontSize: 18, marginBottom: 32 }}>
                            Начните использовать TextAI Pro сегодня и получите 7 дней бесплатного доступа
                        </Paragraph>
                        <Button
                            type="primary"
                            size="large"
                            shape="round"
                            ghost
                            style={{
                                padding: '0 40px',
                                height: 48,
                                fontSize: 16
                            }}
                        >
                            Начать бесплатно <ArrowRightOutlined />
                        </Button>
                    </div>
                </AnimatedCard>

                {/* Contact Form */}
                <Row justify="center" style={{ marginBottom: 80 }}>
                    <Col xs={24} md={16}>
                        <AnimatedCard delay={200}>
                            <Card
                                style={{
                                    borderRadius: 16,
                                    border: 'none',
                                    boxShadow: '0 8px 24px rgba(0,0,0,0.1)'
                                }}
                                bodyStyle={{ padding: 48 }}
                            >
                                <Title level={2} style={{ textAlign: 'center', marginBottom: 8 }}>
                                    Остались вопросы?
                                </Title>
                                <Paragraph style={{ textAlign: 'center', marginBottom: 48 }}>
                                    Оставьте свои контакты, и мы свяжемся с вами в течение 24 часов
                                </Paragraph>
                                <Form
                                    form={form}
                                    name="contact"
                                    onFinish={onFinish}
                                    layout="vertical"
                                >
                                    <Row gutter={24}>
                                        <Col xs={24} md={12}>
                                            <Form.Item
                                                name="name"
                                                label="Ваше имя"
                                                rules={[{ required: true, message: 'Пожалуйста, введите ваше имя' }]}
                                            >
                                                <Input
                                                    prefix={<UserOutlined />}
                                                    placeholder="Имя"
                                                    size="large"
                                                    style={{ borderRadius: 8 }}
                                                />
                                            </Form.Item>
                                        </Col>
                                        <Col xs={24} md={12}>
                                            <Form.Item
                                                name="email"
                                                label="Email"
                                                rules={[
                                                    {
                                                        required: true,
                                                        message: 'Пожалуйста, введите ваш email'
                                                    },
                                                    {
                                                        type: 'email',
                                                        message: 'Пожалуйста, введите корректный email',
                                                    }
                                                ]}
                                            >
                                                <Input
                                                    prefix={<MailOutlined />}
                                                    placeholder="Email"
                                                    size="large"
                                                    style={{ borderRadius: 8 }}
                                                />
                                            </Form.Item>
                                        </Col>
                                    </Row>
                                    <Form.Item
                                        name="message"
                                        label="Сообщение"
                                    >
                                        <Input.TextArea
                                            placeholder="Ваш вопрос или комментарий"
                                            rows={4}
                                            style={{ borderRadius: 8 }}
                                        />
                                    </Form.Item>
                                    <Form.Item>
                                        <Button
                                            type="primary"
                                            htmlType="submit"
                                            block
                                            size="large"
                                            style={{
                                                height: 48,
                                                borderRadius: 8,
                                                fontSize: 16
                                            }}
                                        >
                                            Отправить сообщение
                                        </Button>
                                    </Form.Item>
                                </Form>
                            </Card>
                        </AnimatedCard>
                    </Col>
                </Row>
            </Content>

            <Footer style={{
                textAlign: 'center',
                background: '#001529',
                color: 'rgba(255,255,255,0.85)',
                padding: '48px 24px'
            }}>
                <Row gutter={[48, 48]} justify="center">
                    <Col xs={24} md={8}>
                        <Title level={4} style={{ color: '#fff' }}>
                            <RobotFilled style={{ marginRight: 8 }} />
                            TextAI Pro
                        </Title>
                        <Paragraph>
                            Передовые технологии обработки текста на основе искусственного интеллекта
                        </Paragraph>
                    </Col>
                    <Col xs={24} md={4}>
                        <Title level={5} style={{ color: '#fff' }}>Продукт</Title>
                        <List
                            dataSource={['Возможности', 'Тарифы', 'Демо', 'FAQ']}
                            renderItem={(item) => (
                                <List.Item style={{ padding: '8px 0', justifyContent: 'center' }}>
                                    <a href="#" style={{ color: 'rgba(255,255,255,0.65)' }}>{item}</a>
                                </List.Item>
                            )}
                        />
                    </Col>
                    <Col xs={24} md={4}>
                        <Title level={5} style={{ color: '#fff' }}>Компания</Title>
                        <List
                            dataSource={['О нас', 'Блог', 'Карьера', 'Контакты']}
                            renderItem={(item) => (
                                <List.Item style={{ padding: '8px 0', justifyContent: 'center' }}>
                                    <a href="#" style={{ color: 'rgba(255,255,255,0.65)' }}>{item}</a>
                                </List.Item>
                            )}
                        />
                    </Col>
                    <Col xs={24} md={4}>
                        <Title level={5} style={{ color: '#fff' }}>Правовая информация</Title>
                        <List
                            dataSource={['Условия использования', 'Политика конфиденциальности', 'Cookie']}
                            renderItem={(item) => (
                                <List.Item style={{ padding: '8px 0', justifyContent: 'center' }}>
                                    <a href="#" style={{ color: 'rgba(255,255,255,0.65)' }}>{item}</a>
                                </List.Item>
                            )}
                        />
                    </Col>
                </Row>
                <Divider style={{ borderColor: 'rgba(255,255,255,0.1)' }} />
                <Paragraph style={{ color: 'rgba(255,255,255,0.45)' }}>
                    © {new Date().getFullYear()} TextAI Pro. Все права защищены.
                </Paragraph>
            </Footer>
        </Layout>
    );
};

export default AppLanding;