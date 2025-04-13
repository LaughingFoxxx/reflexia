import React, { useState, useRef, useEffect } from 'react';
import {Layout, Menu, Button, Card, Space, Popover, Spin, message, Modal} from 'antd';
import {
    FileTextOutlined,
    UserOutlined,
    SettingOutlined,
    PlusOutlined,
    ArrowLeftOutlined,
    SaveOutlined,
    LoadingOutlined,
    DeleteOutlined
} from '@ant-design/icons';
import ReactQuill from 'react-quill';
import 'react-quill/dist/quill.snow.css';
import './App.css';
import SettingsPage from "./SettingsPage.jsx";
import AccountPage from "./AccountPage.jsx";

const { Sider, Content } = Layout;

// Вспомогательная функция для удаления HTML-тегов
const stripHtml = (html) => {
    const doc = new DOMParser().parseFromString(html, 'text/html');
    return doc.body.textContent || '';
};

const App = () => {
    const [selectedMenu, setSelectedMenu] = useState('documents');
    const [currentDoc, setCurrentDoc] = useState(null);
    const [editorContent, setEditorContent] = useState('');
    const [selection, setSelection] = useState(null);
    const [processedText, setProcessedText] = useState('');
    const [isProcessing, setIsProcessing] = useState(false);
    const [showPreview, setShowPreview] = useState(false);
    const [isMobile, setIsMobile] = useState(window.innerWidth <= 1100);
    const [documents, setDocuments] = useState([]);
    const [isLoadingDocuments, setIsLoadingDocuments] = useState(false);
    const [fetchError, setFetchError] = useState(null);
    const [isDocumentsFetched, setIsDocumentsFetched] = useState(false);
    const isFetchingRef = useRef(false); // Реф для предотвращения двойного вызова
    const quillRef = useRef(null);
    const [documentToDelete, setDocumentToDelete] = useState(null);

    // Отслеживание изменения размера экрана
    useEffect(() => {
        const handleResize = () => setIsMobile(window.innerWidth <= 1240);
        window.addEventListener('resize', handleResize);
        return () => window.removeEventListener('resize', handleResize);
    }, []);

    // Применение начальных настроек
    useEffect(() => {
        const initialDarkMode = localStorage.getItem('darkMode') === 'true';
        document.body.classList.toggle('dark-theme', initialDarkMode);
        const initialFontSize = localStorage.getItem('editorFontSize') || '16';
        const initialFontFamily = localStorage.getItem('editorFontFamily') || "'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif";
        document.documentElement.style.setProperty('--editor-font-size', `${initialFontSize}px`);
        document.documentElement.style.setProperty('--editor-font-family', initialFontFamily);
    }, []);

    // Функция удаления документа
    const handleDeleteDocument = async (docId) => {
        try {
            const response = await fetch(`http://localhost:8082/api/text/delete-document?documentId=${docId}`, {
                method: 'DELETE',
                credentials: 'include',
            });

            if (response.status === 401) {
                // Обработка обновления токена
                const refreshResponse = await fetch('http://localhost:8082/api/auth/refresh', {
                    method: 'POST',
                    credentials: 'include',
                });

                if (!refreshResponse.ok) {
                    throw new Error('Failed to refresh token');
                }

                // Повторный запрос после обновления токена
                const retryResponse = await fetch(`http://localhost:8082/api/text/delete-document?documentId=${docId}`, {
                    method: 'DELETE',
                    credentials: 'include',
                });

                if (!retryResponse.ok) {
                    throw new Error(`HTTP error ${retryResponse.status}`);
                }
            } else if (!response.ok) {
                throw new Error(`HTTP error ${response.status}`);
            }

            // Успешное удаление
            setDocuments(prevDocs => prevDocs.filter(doc => doc.id !== docId));
            message.success('Документ удален');
            if (currentDoc?.id === docId) {
                setCurrentDoc(null);
                setEditorContent('');
            }
        } catch (error) {
            console.error('Failed to delete document:', error);
            message.error(`Ошибка при удалении документа: ${error.message}`);
        }
    };

    // Подтверждение удаления
    const confirmDelete = (doc) => {
        setDocumentToDelete(doc);
        Modal.confirm({
            title: 'Удалить документ?',
            content: `Вы уверены, что хотите удалить документ "${doc.title}"? Это действие нельзя отменить.`,
            okText: 'Удалить',
            okType: 'danger',
            cancelText: 'Отмена',
            onOk: () => {
                handleDeleteDocument(doc.id);
                setDocumentToDelete(null);
            },
            onCancel: () => {
                setDocumentToDelete(null);
            },
        });
    };

    // Функция для получения документов
    const fetchDocuments = async (isRetryAfterRefresh = false) => {
        if (isFetchingRef.current && !isRetryAfterRefresh) return;
        if (isDocumentsFetched && !isRetryAfterRefresh) return;

        isFetchingRef.current = true;
        setIsLoadingDocuments(true);
        setFetchError(null);

        try {
            const response = await fetch('http://localhost:8082/api/text/all-user-documents', {
                method: 'GET',
                credentials: 'include',
            });

            if (response.status === 401 && !isRetryAfterRefresh) {
                const refreshResponse = await fetch('http://localhost:8082/api/auth/refresh', {
                    method: 'POST',
                    credentials: 'include',
                });

                if (!refreshResponse.ok) {
                    throw new Error('Failed to refresh token');
                }

                return fetchDocuments(true);
            }

            if (!response.ok) {
                throw new Error(`HTTP error ${response.status}: ${response.statusText}`);
            }

            const data = await response.json();
            let documentList = [];

            if (Array.isArray(data)) {
                documentList = data;
            } else if (data?.documents) {
                documentList = data.documents;
            } else if (data?.documentId && data?.documentName) {
                documentList = [data];
            } else {
                throw new Error("Unexpected data structure received from server.");
            }

            const formattedDocuments = documentList.map(doc => {
                if (!doc || typeof doc !== 'object') return null;
                const updatedAtDate = new Date(doc.updatedAt);
                const formattedDate = isNaN(updatedAtDate) ? 'Unknown Date' : updatedAtDate.toISOString().split('T')[0];
                const cleanText = stripHtml(doc.text || ''); // Убираем HTML-теги
                const previewText = cleanText.substring(0, 100) + (cleanText.length > 100 ? '...' : '') || `Content for ${doc.documentName || 'Untitled'}...`;

                return {
                    id: doc.documentId || `missing_id_${Math.random()}`,
                    title: doc.documentName || 'Untitled Document',
                    date: formattedDate,
                    preview: previewText,
                    content: doc.text || '',
                    createdAt: doc.createdAt,
                    updatedAt: doc.updatedAt,
                };
            }).filter(Boolean);

            setDocuments(formattedDocuments);
            setIsDocumentsFetched(true);
        } catch (error) {
            setFetchError(error.message);
            message.error(`Failed to load documents: ${error.message}`);
        } finally {
            setIsLoadingDocuments(false);
            isFetchingRef.current = false;
        }
    };

    // Вызов fetchDocuments только при монтировании или первом переходе на 'documents'
    useEffect(() => {
        if (selectedMenu === 'documents' && !currentDoc && !isDocumentsFetched && !isFetchingRef.current) {
            fetchDocuments();
        }
    }, [selectedMenu, currentDoc]); // Зависимости остаются, но проверка внутри предотвращает лишние вызовы

    const menuItems = [
        { key: 'documents', icon: <FileTextOutlined />, label: 'Мои документы' },
        { key: 'account', icon: <UserOutlined />, label: 'Аккаунт' },
        { key: 'settings', icon: <SettingOutlined />, label: 'Настройки' },
    ];

    const processingOptions = [
        { category: "Улучшение текста", options: ["Повысить ясность", "Сделать короче", "Развить мысль", "Исправить ошибки", "Формальный тон", "Неформальный тон", "Упростить язык"] },
        { category: "Генерация", options: ["Пересказать", "Придумать заголовок", "Найти задачи", "Продолжить писать", "Создать список", "Написать введение", "Написать заключение"] },
        { category: "Анализ и формат", options: ["Извлечь ключевые слова", "Определить тональность", "Форматировать как Email", "Форматировать как Отчет", "Добавить Эмодзи"] },
        { category: "Перевод", options: ["Перевести на Английский", "Перевести на Немецкий", "Перевести на Французский", "Определить язык"] },
        { category: "Другое", options: ["Объяснить это", "Превратить в стих", "Сделать твит", "Связанный факт"] },
    ];

    const handleSelectionChange = () => {
        if (quillRef.current) {
            const quill = quillRef.current.getEditor();
            const range = quill.getSelection();
            if (range && range.length > 0) {
                const text = quill.getText(range.index, range.length);
                setSelection({ text, range });
                setShowPreview(false);
            } else {
                setSelection(null);
            }
        }
    };

    const handleProcessText = async (option) => {
        if (!selection || !selection.text) return;

        setIsProcessing(true);
        setProcessedText(''); // Очищаем предыдущий результат

        // Маппинг опций к промптам
        const prompts = {
            "Повысить ясность": "Перепиши этот текст, чтобы он стал более ясным и понятным, сохранив исходный смысл.",
            "Сделать короче": "Сократи этот текст, сохранив основную идею.",
            "Развить мысль": "Разверни эту мысль, добавив больше деталей и примеров.",
            "Исправить ошибки": "Исправь грамматические, орфографические и пунктуационные ошибки в этом тексте.",
            "Формальный тон": "Перепиши текст в формальном стиле, подходящем для официальных документов.",
            "Неформальный тон": "Перепиши текст в неформальном, разговорном стиле.",
            "Упростить язык": "Упростить язык текста, сделав его доступным для широкой аудитории.",
            "Пересказать": "Составь краткое содержание этого текста.",
            "Придумать заголовок": "Предложи подходящий заголовок для этого текста.",
            "Найти задачи": "Выдели из текста список задач или действий.",
            "Продолжить писать": "Продолжи этот текст, развивая его логично и естественно.",
            "Создать список": "Преобразуй текст в структурированный список.",
            "Написать введение": "Напиши введение для этого текста.",
            "Написать заключение": "Напиши заключение для этого текста.",
            "Извлечь ключевые слова": "Выдели ключевые слова или фразы из этого текста.",
            "Определить тональность": "Определи эмоциональную тональность текста (позитивная, негативная, нейтральная) и объясни почему.",
            "Форматировать как Email": "Преобразуй текст в формат электронного письма.",
            "Форматировать как Отчет": "Преобразуй текст в формат официального отчета.",
            "Добавить Эмодзи": "Добавь подходящие эмодзи к тексту, не меняя его содержания.",
            "Перевести на Английский": "Переведи этот текст на английский язык.",
            "Перевести на Немецкий": "Переведи этот текст на немецкий язык.",
            "Перевести на Французский": "Переведи этот текст на французский язык.",
            "Определить язык": "Определи, на каком языке написан этот текст.",
            "Объяснить это": "Объясни содержание этого текста простыми словами.",
            "Превратить в стих": "Перепиши этот текст в виде стихотворения.",
            "Сделать твит": "Преобразуй текст в сообщение длиной до 280 символов, подходящее для твита.",
            "Связанный факт": "Найди интересный факт, связанный с содержанием этого текста.",
        };

        const instruction = prompts[option] || "Обработай этот текст в соответствии с выбранной опцией.";

        try {
            const payload = {
                instruction: instruction,
                text: selection.text,
            };

            const response = await fetch('http://localhost:8082/api/text/process-text', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(payload),
                credentials: 'include', // Отправляем токены в куках
            });

            if (response.status === 401) {
                // Обработка обновления токена
                const refreshResponse = await fetch('http://localhost:8082/api/auth/refresh', {
                    method: 'POST',
                    credentials: 'include',
                });

                if (!refreshResponse.ok) {
                    throw new Error('Failed to refresh token');
                }

                // Повторный запрос после обновления токена
                const retryResponse = await fetch('http://localhost:8082/api/text/process-text', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(payload),
                    credentials: 'include',
                });

                if (!retryResponse.ok) {
                    throw new Error(`HTTP error ${retryResponse.status}`);
                }

                const retryData = await retryResponse.json();
                setProcessedText(retryData.result || 'Ошибка: пустой результат');
            } else if (!response.ok) {
                throw new Error(`HTTP error ${response.status}`);
            } else {
                const data = await response.json();
                setProcessedText(data.result || 'Ошибка: пустой результат');
            }

            setShowPreview(true);
        } catch (error) {
            console.error('Failed to process text:', error);
            setProcessedText(`Ошибка обработки: ${error.message}`);
            setShowPreview(true);
        } finally {
            setIsProcessing(false);
        }
    };

    const handleReplaceText = () => {
        if (quillRef.current && selection) {
            const quill = quillRef.current.getEditor();
            quill.deleteText(selection.range.index, selection.range.length);
            quill.insertText(selection.range.index, processedText);
            setShowPreview(false);
            setSelection(null);
        }
    };

    const processingContent = (
        <div className={`processing-menu ${showPreview ? 'wide-preview' : ''}`}>
            {showPreview ? (
                <div className="preview-container">
                    <div className="preview-text">
                        {/* Рендерим текст с учетом форматирования */}
                        <div
                            dangerouslySetInnerHTML={{
                                __html: processedText
                                    .replace(/\n/g, '<br/>') // Преобразуем переносы строк в <br/>
                                    .replace(/\r\n/g, '<br/>') // Для Windows-формата
                            }}
                        />
                    </div>
                    <div className="preview-buttons">
                        <Button onClick={() => setShowPreview(false)} style={{ marginRight: 8 }}>
                            Назад
                        </Button>
                        <Button type="primary" onClick={handleReplaceText}>
                            Заменить
                        </Button>
                    </div>
                </div>
            ) : isProcessing ? (
                <div className="spinner-container">
                    <Spin />
                </div>
            ) : (
                <div className="options-container">
                    {processingOptions.map((section, index) => (
                        <div key={index}>
                            <div className="processing-category">{section.category}</div>
                            <div className="processing-options">
                                {section.options.map(option => (
                                    <Button
                                        key={option}
                                        className="processing-option"
                                        type="text"
                                        onClick={() => handleProcessText(option)}
                                    >
                                        {option}
                                    </Button>
                                ))}
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );

    const handleCreateDocument = async () => {
        const newDocId = `new_${Date.now()}`; // Генерируем временный ID
        const initialTitle = 'Новый документ';
        const initialContent = '';

        // Создаем новый документ локально
        const newDoc = {
            id: newDocId,
            title: initialTitle,
            content: initialContent,
            date: new Date().toISOString().split('T')[0],
            preview: 'Начните печатать...',
            isNew: true,
        };

        // Отправляем запрос на сервер
        try {
            const payload = {
                documentId: newDocId,
                documentName: initialTitle,
                text: initialContent,
            };

            const response = await fetch('http://localhost:8082/api/text/create-new-document', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(payload),
                credentials: 'include',
            });

            if (response.status === 401) {
                // Обработка обновления токена
                const refreshResponse = await fetch('http://localhost:8082/api/auth/refresh', {
                    method: 'POST',
                    credentials: 'include',
                });

                if (!refreshResponse.ok) {
                    throw new Error('Failed to refresh token');
                }

                // Повторяем запрос после обновления токена
                const retryResponse = await fetch('http://localhost:8082/api/text/create-new-document', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(payload),
                    credentials: 'include',
                });

                if (!retryResponse.ok) {
                    throw new Error(`HTTP error ${retryResponse.status}`);
                }
            } else if (!response.ok) {
                throw new Error(`HTTP error ${response.status}`);
            }

            // Если сервер вернул данные, обновляем локальный документ
            const contentType = response.headers.get('Content-Type');
            let serverDoc = null;
            if (contentType && contentType.includes('application/json') && response.status !== 204) {
                serverDoc = await response.json();
            }

            const updatedDoc = {
                ...newDoc,
                id: serverDoc?.documentId || newDocId,
                title: serverDoc?.documentName || initialTitle,
                content: serverDoc?.text || initialContent,
                updatedAt: serverDoc?.updatedAt || new Date().toISOString(),
                isNew: false,
            };

            // Обновляем состояние
            setCurrentDoc(updatedDoc);
            setEditorContent(initialContent);
            setDocuments(prevDocs => [updatedDoc, ...prevDocs]);
            message.success('Документ создан');

        } catch (error) {
            console.error('Failed to create document:', error);
            message.error(`Ошибка при создании документа: ${error.message}`);
            // Все равно показываем документ локально, даже если запрос не удался
            setCurrentDoc(newDoc);
            setEditorContent(initialContent);
            setDocuments(prevDocs => [newDoc, ...prevDocs]);
        }
    };

    const handleOpenDocument = (doc) => {
        setCurrentDoc(doc);
        setEditorContent(doc.content);
        setSelection(null);
    };

    const handleSave = async () => {
        if (!currentDoc) return;

        message.info('Сохранение...');
        setIsLoadingDocuments(true);

        const payload = {
            documentId: currentDoc.id,
            documentName: currentDoc.title,
            text: editorContent,
        };

        const saveDocument = async (isRetryAfterRefresh = false) => {
            try {
                const response = await fetch('http://localhost:8082/api/text/save-document-changes', {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(payload),
                    credentials: 'include',
                });

                if (response.status === 401 && !isRetryAfterRefresh) {
                    const refreshResponse = await fetch('http://localhost:8082/api/auth/refresh', {
                        method: 'POST',
                        credentials: 'include',
                    });

                    if (!refreshResponse.ok) {
                        throw new Error('Failed to refresh token');
                    }

                    return saveDocument(true);
                }

                if (!response.ok) {
                    const errorText = await response.text();
                    throw new Error(`HTTP error ${response.status}: ${errorText || 'Failed to save document'}`);
                }

                const contentType = response.headers.get('Content-Type');
                let updatedDocFromServer = null;

                if (contentType && contentType.includes('application/json') && response.status !== 204) {
                    updatedDocFromServer = await response.json();
                    console.log('Server response:', updatedDocFromServer);
                } else {
                    console.log('No JSON response from server, using local data');
                }

                const cleanText = stripHtml(updatedDocFromServer?.text || editorContent); // Убираем HTML-теги
                const updatedDoc = {
                    ...currentDoc,
                    title: updatedDocFromServer?.documentName || currentDoc.title,
                    content: updatedDocFromServer?.text || editorContent,
                    date: new Date(updatedDocFromServer?.updatedAt || Date.now()).toISOString().split('T')[0],
                    preview: cleanText.substring(0, 100) + (cleanText.length > 100 ? '...' : ''),
                    updatedAt: updatedDocFromServer?.updatedAt || currentDoc.updatedAt || new Date().toISOString(),
                    isNew: false,
                };

                setCurrentDoc(updatedDoc);
                setDocuments(prevDocs =>
                    prevDocs.map(doc =>
                        doc.id === updatedDoc.id ? updatedDoc : doc
                    )
                );

                message.success('Документ сохранен');
            } catch (error) {
                console.error('Failed to save document:', error);
                message.error(`Ошибка сохранения: ${error.message}`);
            } finally {
                setIsLoadingDocuments(false);
            }
        };

        await saveDocument();
    };

    const renderEditor = () => (
        <div className="editor-container">
            <div className="editor-header">
                <Button icon={<ArrowLeftOutlined />} onClick={() => setCurrentDoc(null)} type="text" className="back-button">Назад</Button>
                <div className="title-wrapper">
                    <input
                        type="text"
                        value={currentDoc?.title || ''}
                        onChange={(e) => setCurrentDoc(prev => ({ ...prev, title: e.target.value }))}
                        className="document-title-input"
                        placeholder="Название документа"
                    />
                </div>
                <Button icon={<SaveOutlined />} onClick={handleSave} type="primary" className="save-button" disabled={!currentDoc || isLoadingDocuments}>
                    Сохранить
                </Button>
            </div>
            <div className="editor-wrapper">
                <Popover
                    content={processingContent}
                    trigger="click"
                    open={selection !== null}
                    placement={isMobile ? 'bottom' : 'leftTop'}
                    overlayClassName="processing-popover"
                >
                    <ReactQuill
                        ref={quillRef}
                        theme="snow"
                        value={editorContent}
                        onChange={setEditorContent}
                        onChangeSelection={handleSelectionChange}
                        className="custom-quill"
                    />
                </Popover>
            </div>
        </div>
    );

    const renderDocuments = () => {
        if (isLoadingDocuments) return <div className="centered-spin"><Spin indicator={<LoadingOutlined style={{ fontSize: 36 }} spin />} tip="Loading documents..." /></div>;
        if (fetchError) return <div className="centered-error">Error loading documents: {fetchError} <Button onClick={() => fetchDocuments()}>Retry</Button></div>;

        return (
            <>
                <Button type="primary" icon={<PlusOutlined />} onClick={handleCreateDocument} className="create-doc-btn">Создать документ</Button>
                {documents.length === 0 ? (
                    <div className="no-documents-message">Нет сохраненных документов. Нажмите "Создать документ", чтобы начать.</div>
                ) : (
                    <div className="documents-grid">
                        {documents.map(doc => (
                            <div key={doc.id} className="document-card">
                                <DeleteOutlined
                                    className="delete-icon"
                                    onClick={(e) => {
                                        e.stopPropagation(); // Предотвращаем открытие документа при клике на иконку
                                        confirmDelete(doc);
                                    }}
                                />
                                <div className="document-card-content" onClick={() => handleOpenDocument(doc)}>
                                    <div className="document-title">{doc.title}</div>
                                    <div className="document-preview">{doc.preview}</div>
                                    <div className="document-date">Последнее изменение: {doc.date}</div>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </>
        );
    };

    const renderContent = () => {
        if (currentDoc) return renderEditor();
        switch (selectedMenu) {
            case 'documents': return renderDocuments();
            case 'account': return <AccountPage />;
            case 'settings': return <SettingsPage />;
            default: return <div style={{ padding: 24 }}>Select a section</div>;
        }
    };

    return (
        <Layout style={{ minHeight: '100vh' }}>
            <Sider width={250} className="app-sider">
                <div className="logo">Рефлексия AI</div>
                <Menu
                    theme={document.body.classList.contains('dark-theme') ? 'dark' : 'light'}
                    mode="inline"
                    selectedKeys={[selectedMenu]}
                    items={menuItems}
                    onClick={(e) => {
                        setSelectedMenu(e.key);
                        setCurrentDoc(null);
                        setSelection(null);
                        setShowPreview(false);
                    }}
                    style={{ borderRight: 0, fontSize: '16px' }}
                />
            </Sider>
            <Layout>
                <Content style={{ padding: '24px', margin: 0, minHeight: 280 }}>
                    {renderContent()}
                </Content>
            </Layout>
        </Layout>
    );
};

export default App;