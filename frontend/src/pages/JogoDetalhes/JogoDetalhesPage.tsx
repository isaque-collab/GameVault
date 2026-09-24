import {useEffect, useState} from 'react'
import {useParams} from 'react-router'
import {buscarDetalhesJogo} from '../../api/jogosApi.ts'
import type {JogoDetalhes} from '../../types/jogo.ts'
import './JogoDetalhesPage.css'

function JogoDetalhesPage() {
    const {rawgGameId} = useParams()

    const [jogo, setJogo] = useState<JogoDetalhes | null>(null)
    const [carregando, setCarregando] = useState(true)
    const [erro, setErro] = useState<string | null>(null)

    useEffect(() => {
        async function carregarJogo() {
            const id = Number(rawgGameId)

            if (!rawgGameId || Number.isNaN(id) || id < 1) {
                setErro('Identificador de jogo inválido.')
                setCarregando(false)
                return
            }

            try {
                setCarregando(true)
                setErro(null)

                const resposta = await buscarDetalhesJogo(id)

                setJogo(resposta)
            } catch (error) {
                console.error(error)
                setErro('Não foi possível carregar os detalhes do jogo.')
            } finally {
                setCarregando(false)
            }
        }

        carregarJogo()
    }, [rawgGameId])

    if (carregando) {
        return (
            <main className="jogo-detalhes">
                <p>Carregando jogo...</p>
            </main>
        )
    }

    if (erro) {
        return (
            <main className="jogo-detalhes">
                <p>{erro}</p>
            </main>
        )
    }

    if (!jogo) {
        return null
    }

    return (
        <main className="jogo-detalhes">
            {jogo.imagemFundo && (
                <img
                    className="jogo-detalhes_imagem"
                    src={jogo.imagemFundo}
                    alt={`Imagem de ${jogo.nome}`}
                />
            )}

            <h1>{jogo.nome}</h1>

            {jogo.dataLancamento && (
                <p>
                    <strong>Lançamento:</strong>{' '}
                    {jogo.dataLancamento}
                </p>
            )}

            {jogo.notaRawg !== null && (
                <p>
                    <strong>Avaliação RAWG:</strong>{' '}
                    {jogo.notaRawg}
                </p>
            )}

            {jogo.metacritic !== null && (
                <p>
                    <strong>Metacritic:</strong>{' '}
                    {jogo.metacritic}
                </p>
            )}

            {jogo.tempoMedioJogo !== null && (
                <p>
                    <strong>Tempo médio:</strong>{' '}
                    {jogo.tempoMedioJogo} horas
                </p>
            )}

            {jogo.classificacaoEtaria && (
                <p>
                    <strong>Classificação etária:</strong>{' '}
                    {jogo.classificacaoEtaria}
                </p>
            )}

            {jogo.generos.length > 0 && (
                <section>
                    <h2>Gêneros</h2>
                    <p>{jogo.generos.join(', ')}</p>
                </section>
            )}

            {jogo.plataformas.length > 0 && (
                <section>
                    <h2>Plataformas</h2>

                    <ul>
                        {jogo.plataformas.map((plataforma) => (
                            <li key={plataforma.id}>
                                {plataforma.nome}
                            </li>
                        ))}
                    </ul>
                </section>
            )}

            {jogo.desenvolvedoras.length > 0 && (
                <section>
                    <h2>Desenvolvedoras</h2>
                    <p>{jogo.desenvolvedoras.join(', ')}</p>
                </section>
            )}

            {jogo.publicadoras.length > 0 && (
                <section>
                    <h2>Publicadoras</h2>
                    <p>{jogo.publicadoras.join(', ')}</p>
                </section>
            )}

            {jogo.descricao && (
                <section>
                    <h2>Descrição</h2>
                    <p>{jogo.descricao}</p>
                </section>
            )}

            {jogo.screenshots.length > 0 && (
                <section>
                    <h2>Screenshots</h2>

                    <div className="jogo-detalhes__screenshots">
                        {jogo.screenshots.map((screenshot) => (
                            <img
                                key={screenshot}
                                src={screenshot}
                                alt={`Screenshot de ${jogo.nome}`}
                            />
                        ))}
                    </div>
                </section>
            )}
        </main>
    )
}

export default JogoDetalhesPage