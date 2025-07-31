const API_BASE_URL = '';

async function apiRequest(url, options = {}) {
    const token = localStorage.getItem('token');
    
    if (!token && !url.includes('/api/auth/')) {
        window.location.href = 'index.html';
        throw new Error('Необходима авторизация');
    }
    
    const config = {
        headers: {
            'Content-Type': 'application/json',
            ...(token && { 'Authorization': `Bearer ${token}` })
        },
        ...options
    };

    try {
        const response = await fetch(API_BASE_URL + url, config);
        
        if ((response.status === 401 || response.status === 403) && 
            !url.includes('/api/auth/login') && !url.includes('/api/auth/register')) {
            localStorage.clear();
            window.location.href = 'index.html';
            return;
        }
        
        if (response.status === 204 || response.headers.get('content-length') === '0') {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return null;
        }
        
        const contentType = response.headers.get('content-type');
        let data;
        
        if (contentType && contentType.includes('application/json')) {
            data = await response.json();
        } else {
            data = await response.text();
        }
        
        if (!response.ok) {
            const errorMessage = typeof data === 'string' ? data : 
                                (data.message || data.error || `HTTP error! status: ${response.status}`);
            throw new Error(errorMessage);
        }
        
        return data;
    } catch (error) {
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            throw new Error('Ошибка соединения с сервером. Проверьте, что сервер запущен.');
        }
        throw error;
    }
}

function showAlert(message, type = 'info') {
    const alertDiv = document.getElementById('alert');
    if (!alertDiv) return;
    
    alertDiv.className = `alert alert-${type}`;
    alertDiv.textContent = message;
    alertDiv.classList.remove('d-none');
    
    setTimeout(() => {
        alertDiv.classList.add('d-none');
    }, 5000);
}

function checkAuth() {
    const token = localStorage.getItem('token');
    const currentPage = window.location.pathname.split('/').pop();
    
    if (!token && currentPage !== 'index.html' && currentPage !== '') {
        window.location.href = 'index.html';
        return false;
    }
    
    return true;
}

function logout() {
    if (confirm('Вы уверены, что хотите выйти?')) {
        localStorage.clear();
        window.location.href = 'index.html';
    }
}

function showModal(modalId) {
    const modalElement = document.getElementById(modalId);
    if (modalElement) {
        try {
            if (typeof bootstrap !== 'undefined' && bootstrap.Modal) {
                const modal = new bootstrap.Modal(modalElement);
                modal.show();
            } else {
                modalElement.style.display = 'block';
                modalElement.classList.add('show');
                document.body.classList.add('modal-open');
                
                const backdrop = document.createElement('div');
                backdrop.className = 'modal-backdrop fade show';
                backdrop.id = modalId + '-backdrop';
                document.body.appendChild(backdrop);
            }
        } catch (error) {
            console.error('Error showing modal:', error);
        }
    }
}

function hideModal(modalId) {
    const modalElement = document.getElementById(modalId);
    if (modalElement) {
        try {
            if (typeof bootstrap !== 'undefined' && bootstrap.Modal) {
                const modalInstance = bootstrap.Modal.getInstance(modalElement);
                if (modalInstance) {
                    modalInstance.hide();
                } else {
                    const modal = new bootstrap.Modal(modalElement);
                    modal.hide();
                }
            } else {            
                modalElement.style.display = 'none';
                modalElement.classList.remove('show');
                document.body.classList.remove('modal-open');
                
                const backdrop = document.getElementById(modalId + '-backdrop');
                if (backdrop) {
                    backdrop.remove();
                }
            }
        } catch (error) {
            console.error('Error hiding modal:', error);
            modalElement.style.display = 'none';
            modalElement.classList.remove('show');
            document.body.classList.remove('modal-open');
            const backdrop = document.querySelector('.modal-backdrop');
            if (backdrop) backdrop.remove();
        }
    }
}

window.addEventListener('error', (event) => {
    console.error('JavaScript error:', event.error);
});

window.addEventListener('unhandledrejection', (event) => {
    console.error('Unhandled promise rejection:', event.reason);
});

const style = document.createElement('style');
style.textContent = `
    .card {
        transition: transform 0.2s ease-in-out;
    }
    
    .card:hover {
        transform: translateY(-2px);
    }
    
    .btn {
        transition: all 0.2s ease-in-out;
    }
    
    .table th {
        background-color: #f8f9fa;
        font-weight: 600;
    }
    
    .alert {
        border-radius: 8px;
    }
    
    code {
        background-color: #f8f9fa;
        padding: 2px 4px;
        border-radius: 4px;
        font-family: 'Courier New', monospace;
    }
    
    .navbar-brand {
        font-weight: bold;
    }
    
    .modal-header {
        background-color: #f8f9fa;
        border-bottom: 1px solid #dee2e6;
    }
    
    .form-control:focus {
        border-color: #0d6efd;
        box-shadow: 0 0 0 0.2rem rgba(13, 110, 253, 0.25);
    }
`;

document.head.appendChild(style);

document.addEventListener('DOMContentLoaded', function() {
    document.addEventListener('click', function(event) {
        if (event.target.hasAttribute('data-bs-dismiss') && event.target.getAttribute('data-bs-dismiss') === 'modal') {
            const modal = event.target.closest('.modal');
            if (modal) {
                hideModal(modal.id);
            }
        }
        
        if (event.target.classList.contains('modal')) {
            hideModal(event.target.id);
        }
    });
    
    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape') {
            const openModal = document.querySelector('.modal.show');
            if (openModal) {
                hideModal(openModal.id);
            }
        }
    });
});