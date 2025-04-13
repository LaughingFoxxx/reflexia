// SettingsPage.js
import React, { useState, useEffect } from 'react';
import { Card, Switch, Select, Radio, Typography, Space, Form, Row, Col } from 'antd';
import './SettingsPage.css'; // We'll create this CSS file next

const { Title, Text } = Typography;
const { Option } = Select;

// Define available font families (add more as needed)
const fontFamilies = [
    { value: "'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif", label: 'System Default' },
    { value: 'Arial, sans-serif', label: 'Arial' },
    { value: 'Verdana, sans-serif', label: 'Verdana' },
    { value: 'Times New Roman, serif', label: 'Times New Roman' },
    { value: 'Georgia, serif', label: 'Georgia' },
    { value: 'Courier New, monospace', label: 'Courier New' },
];

// Define available font sizes
const fontSizes = [12, 14, 16, 18, 20, 24];

const SettingsPage = () => {
    // --- State ---
    // Initialize state with values from localStorage or defaults
    const [darkMode, setDarkMode] = useState(() => localStorage.getItem('darkMode') === 'true' || false);
    const [language, setLanguage] = useState(() => localStorage.getItem('language') || 'ru');
    const [editorFontSize, setEditorFontSize] = useState(() => parseInt(localStorage.getItem('editorFontSize') || '16', 10));
    const [editorFontFamily, setEditorFontFamily] = useState(() => localStorage.getItem('editorFontFamily') || fontFamilies[0].value);

    // --- Effects ---
    // Apply dark mode class to body and save preference
    useEffect(() => {
        if (darkMode) {
            document.body.classList.add('dark-theme');
            localStorage.setItem('darkMode', 'true');
        } else {
            document.body.classList.remove('dark-theme');
            localStorage.setItem('darkMode', 'false');
        }
    }, [darkMode]);

    // Apply editor font styles using CSS variables and save preferences
    useEffect(() => {
        document.documentElement.style.setProperty('--editor-font-size', `${editorFontSize}px`);
        localStorage.setItem('editorFontSize', editorFontSize.toString());
    }, [editorFontSize]);

    useEffect(() => {
        document.documentElement.style.setProperty('--editor-font-family', editorFontFamily);
        localStorage.setItem('editorFontFamily', editorFontFamily);
    }, [editorFontFamily]);

    // Save language preference
    useEffect(() => {
        localStorage.setItem('language', language);
        // In a real app, you'd trigger a language change here using an i18n library
        console.log(`Language changed to: ${language}. Implement actual i18n logic.`);
    }, [language]);


    // --- Handlers ---
    const handleThemeChange = (checked) => {
        setDarkMode(checked);
    };

    const handleLanguageChange = (e) => {
        setLanguage(e.target.value);
    };

    const handleFontSizeChange = (value) => {
        setEditorFontSize(value);
    };

    const handleFontFamilyChange = (value) => {
        setEditorFontFamily(value);
    };

    // --- Text Content (for basic language switching demo) ---
    const texts = {
        ru: {
            title: 'Настройки',
            appearance: 'Внешний вид',
            darkMode: 'Темная тема',
            language: 'Язык интерфейса',
            editorSettings: 'Настройки редактора',
            fontSize: 'Размер шрифта',
            fontFamily: 'Стиль шрифта',
            russian: 'Русский',
            english: 'English',
        },
        en: {
            title: 'Settings',
            appearance: 'Appearance',
            darkMode: 'Dark Mode',
            language: 'Interface Language',
            editorSettings: 'Editor Settings',
            fontSize: 'Font Size',
            fontFamily: 'Font Style',
            russian: 'Русский',
            english: 'English',
        },
    };

    const currentTexts = texts[language];

    // --- Render ---
    return (
        <div className="settings-page-container">
            <Title level={2} style={{ marginBottom: '24px' }}>{currentTexts.title}</Title>

            {/* Appearance Section */}
            <Card
                title={<Title level={4} style={{ margin: 0 }}>{currentTexts.appearance}</Title>}
                style={{ marginBottom: '24px' }} // Убедимся, что есть отступ снизу
            >
                <Form layout="vertical">
                    {/* Используем Form.Item для строки с темной темой */}
                    <Form.Item label={currentTexts.darkMode} style={{ marginBottom: '16px' }}> {/* Добавляем отступ */}
                        <Row justify="space-between" align="middle"> {/* Распределяем элементы по краям */}
                            <Col span={18}> {/* Даем тексту больше места */}
                                <Text type="secondary">Включает темное оформление интерфейса.</Text>
                            </Col>
                            <Col>
                                <Switch checked={darkMode} onChange={handleThemeChange} />
                            </Col>
                        </Row>
                    </Form.Item>

                    {/* Отдельный Form.Item для языка */}
                    <Form.Item label={currentTexts.language} style={{ marginBottom: 0 }}> {/* Убираем лишний нижний отступ у этого Form.Item */}
                        <Radio.Group onChange={handleLanguageChange} value={language}>
                            <Radio.Button value="ru">{currentTexts.russian}</Radio.Button>
                            <Radio.Button value="en">{currentTexts.english}</Radio.Button>
                        </Radio.Group>
                    </Form.Item>
                </Form>
            </Card>

            {/* Editor Settings Section */}
            <Card title={<Title level={4} style={{ margin: 0 }}>{currentTexts.editorSettings}</Title>}>
                <Form layout="vertical">
                    <Form.Item label={currentTexts.fontSize} style={{ marginBottom: '16px' }}> {/* Добавляем отступ */}
                        <Select
                            value={editorFontSize}
                            style={{ width: 120 }}
                            onChange={handleFontSizeChange}
                        >
                            {fontSizes.map(size => (
                                <Option key={size} value={size}>{size} px</Option>
                            ))}
                        </Select>
                    </Form.Item>
                    <Form.Item label={currentTexts.fontFamily} style={{ marginBottom: 0 }}> {/* Убираем лишний нижний отступ у последнего Form.Item */}
                        <Select
                            value={editorFontFamily}
                            style={{ width: 200 }}
                            onChange={handleFontFamilyChange}
                        >
                            {fontFamilies.map(font => (
                                <Option key={font.value} value={font.value} style={{ fontFamily: font.value }}>
                                    {font.label}
                                </Option>
                            ))}
                        </Select>
                    </Form.Item>
                </Form>
            </Card>
        </div>
    );
};

export default SettingsPage;