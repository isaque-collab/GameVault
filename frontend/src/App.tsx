import { Route, Routes } from 'react-router'
import Header from './components/layout/Header/Header';
import FavoritosPage from './pages/Favoritos/FavoritosPage.tsx';
import HomePage from "./pages/Home/HomePage.tsx";
import ListaDesejosPage from './pages/ListaDesejos/ListaDesejosPage.tsx';
import LoginPage from './pages/Login/LoginPage.tsx';
import NotFoundPage from './pages/NotFound/NotFoundPage.tsx';

function App() {
    return (
        <>
            <Header/>

            <Routes>
                <Route path="/" element={<HomePage />} />
                <Route path="favoritos" element={<FavoritosPage />} />
                <Route path="/lista-desejos" element={<ListaDesejosPage />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="*" element={<NotFoundPage />} />
            </Routes>
        </>
    )
}

export default App
