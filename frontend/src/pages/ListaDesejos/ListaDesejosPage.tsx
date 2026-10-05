import {Link} from 'react-router'
import { useEffect, useState } from 'react'
import { listarListaDesejos } from '../../api/colecoesApi'
import GameSection, {
    type GameSectionItem,
} from '../../components/game/GameSection/GameSection'
import { converterItemColecaoParaGameSectionItem } from '../../mappers/jogoMapper'
import '../colecao.css'

function ListaDesejosPage() {
    const [jogos, setJogos] =
        useState<GameSectionItem[]>([])

    const [carregando, setCarregando] =
        useState(true)

    const [erro, setErro] =
        useState<string | null>(null)

    useEffect(() => {
        async function carregarListaDesejos() {
            try {
                setCarregando(true)
                setErro(null)

                const resposta =
                    await listarListaDesejos()

                setJogos(
                    resposta.map(
                        converterItemColecaoParaGameSectionItem,
                    ),
                )
            } catch (error) {
                console.error(error)

                setErro(
                    'Não foi possível carregar sua lista de desejos.',
                )
            } finally {
                setCarregando(false)
            }
        }

        carregarListaDesejos()
    }, [])

    if (carregando) {
        return (
            <main className="colecao-page">
                <p>Carregando lista de desejos...</p>
            </main>
        )
    }

    if (erro) {
        return (
            <main className="colecao-page">
                <p role="alert">{erro}</p>
            </main>
        )
    }

    return (
        <main className="colecao-page">
            <div className="colecao-cabecalho">
                <h1>Lista de Desejos</h1>
                {jogos.length > 0 && (
                    <span className="colecao-contagem">{jogos.length}</span>
                )}
            </div>

            {jogos.length > 0 ? (
                <GameSection
                    titulo="Sua lista de desejos"
                    jogos={jogos}
                />
            ) : (
                <div className="colecao-vazio">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"
                         strokeWidth="1.8" strokeLinejoin="round" aria-hidden="true">
                        <path d="M5 4h14v17l-7-4-7 4V4Z"/>
                    </svg>
                    <p>Você ainda não adicionou jogos aos desejos.</p>
                    <Link to="/catalogo">Explorar catálogo</Link>
                </div>
            )}
        </main>
    )
}

export default ListaDesejosPage